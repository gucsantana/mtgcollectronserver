package com.gucsantana.mtgcollectronserver.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.models.DatabaseSet;
import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;
import com.gucsantana.mtgcollectronserver.repos.DatabaseSetRepository;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

@Service
public class CollectionBackupImportService {

    private final OwnedCardRepository ownedCardRepository;
    private final UserSetInfoRepository userSetInfoRepository;
    private final DatabaseSetRepository databaseSetRepository;
    private final DatabaseCardRepository databaseCardRepository;

    public CollectionBackupImportService(
            OwnedCardRepository ownedCardRepository,
            UserSetInfoRepository userSetInfoRepository,
            DatabaseSetRepository databaseSetRepository,
            DatabaseCardRepository databaseCardRepository) {
        this.ownedCardRepository = ownedCardRepository;
        this.userSetInfoRepository = userSetInfoRepository;
        this.databaseSetRepository = databaseSetRepository;
        this.databaseCardRepository = databaseCardRepository;
    }

    @Transactional
    @CacheEvict(value = { "owned-card-cache", "user-set-info-cache" }, allEntries = true)
    public ImportResult importBackup(
            byte[] csvContents,
            Long userId,
            boolean additiveImport) {
        if (csvContents.length == 0) {
            throw badRequest("A non-empty backup file is required");
        }

        CollectionBackupCsvParser.ParsedBackup backup;
        try {
            backup = CollectionBackupCsvParser.parse(new ByteArrayInputStream(csvContents));
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read the uploaded backup file", exception);
        }

        List<OwnedCard> importedCards = backup.cards().stream()
                .map(row -> toOwnedCard(row, userId))
                .toList();
        List<UserSetInfo> importedSets = backup.sets().stream()
                .map(row -> toUserSetInfo(row, userId))
                .toList();

        if (additiveImport) {
            importAdditively(userId, importedCards, importedSets);
        } else {
            ownedCardRepository.deleteByCardOwnerId(userId);
            userSetInfoRepository.deleteByCardOwnerId(userId);
            ownedCardRepository.saveAll(importedCards);
            userSetInfoRepository.saveAll(importedSets);
        }
        return new ImportResult(importedSets.size(), importedCards.size());
    }

    private void importAdditively(
            Long userId,
            List<OwnedCard> importedCards,
            List<UserSetInfo> importedSets) {
        List<OwnedCard> existingCards = ownedCardRepository.findByCardOwnerId(userId);
        Map<CollectionBackupCsvParser.CardKey, OwnedCard> mergedByKey = new HashMap<>();
        for (OwnedCard existing : existingCards) {
            CollectionBackupCsvParser.CardKey key =
                    new CollectionBackupCsvParser.CardKey(existing.getSetCode(), existing.getCollectorNumber());
            mergedByKey.put(key, existing);
        }

        for (OwnedCard imported : importedCards) {
            CollectionBackupCsvParser.CardKey key =
                    new CollectionBackupCsvParser.CardKey(imported.getSetCode(), imported.getCollectorNumber());
            OwnedCard existing = mergedByKey.get(key);
            if (existing == null) {
                mergedByKey.put(key, imported);
                continue;
            }
            if (existing.getCardCount() == null) {
                throw badRequest("An existing card has no count and cannot be merged");
            }
            try {
                imported.setCardCount(Math.addExact(existing.getCardCount(), imported.getCardCount()));
            } catch (ArithmeticException exception) {
                throw badRequest("The merged card count exceeds the supported range");
            }
            mergedByKey.put(key, imported);
        }

        List<OwnedCard> mergedCards = new ArrayList<>(mergedByKey.values());
        List<UserSetInfo> recalculatedSets = recalculateSetInfo(
                userId, mergedCards, importedSets);
        ownedCardRepository.saveAll(importedCards);
        userSetInfoRepository.saveAll(recalculatedSets);
    }

    private List<UserSetInfo> recalculateSetInfo(
            Long userId,
            List<OwnedCard> mergedCards,
            List<UserSetInfo> importedSets) {
        Set<String> allSetCodes = new HashSet<>();
        for (OwnedCard card : mergedCards) {
            allSetCodes.add(card.getSetCode());
        }
        for (UserSetInfo setInfo : userSetInfoRepository.findByCardOwnerId(userId)) {
            allSetCodes.add(setInfo.getSetCode());
        }
        for (UserSetInfo setInfo : importedSets) {
            allSetCodes.add(setInfo.getSetCode());
        }

        Map<String, DatabaseSet> databaseSets = new HashMap<>();
        for (DatabaseSet databaseSet : databaseSetRepository.findAllById(allSetCodes)) {
            databaseSets.put(databaseSet.getCode(), databaseSet);
        }
        for (String setCode : allSetCodes) {
            if (!databaseSets.containsKey(setCode)) {
                throw badRequest("Cannot recalculate set info: set " + setCode
                        + " is missing from the database set catalog");
            }
        }

        Map<CollectionBackupCsvParser.CardKey, String> raritiesByCard = new HashMap<>();
        Map<String, RarityTotals> rarityTotalsBySet = new HashMap<>();
        if (!allSetCodes.isEmpty()) {
            for (DatabaseCard databaseCard : databaseCardRepository.findBySetCodeIn(allSetCodes)) {
                String rarity = databaseCard.getRarity().toLowerCase(Locale.ROOT);
                CollectionBackupCsvParser.CardKey key =
                        new CollectionBackupCsvParser.CardKey(
                                databaseCard.getSetCode(), databaseCard.getCollectorNumber());
                raritiesByCard.putIfAbsent(key, rarity);
                RarityTotals totals = rarityTotalsBySet.computeIfAbsent(
                        databaseCard.getSetCode(), ignored -> new RarityTotals());
                totals.addTotal(rarity);
            }
        }

        Map<String, SetTotals> ownedTotalsBySet = new HashMap<>();
        for (OwnedCard card : mergedCards) {
            if (card.getCardCount() == null || card.getCardCount() < 0) {
                throw badRequest("An existing card has an invalid count and cannot be recalculated");
            }
            SetTotals totals = ownedTotalsBySet.computeIfAbsent(
                    card.getSetCode(), ignored -> new SetTotals());
            long count = card.getCardCount();
            if (Boolean.TRUE.equals(card.getIsConsideredExtra())) {
                totals.extraOwned = addCounts(totals.extraOwned, count);
            } else {
                totals.baseOwned = addCounts(totals.baseOwned, count);
            }
            if (Boolean.TRUE.equals(card.getIsFoil())) {
                totals.foilsOwned = addCounts(totals.foilsOwned, count);
            }

            String rarity = raritiesByCard.get(new CollectionBackupCsvParser.CardKey(
                    card.getSetCode(), card.getCollectorNumber()));
            if (rarity == null) {
                throw badRequest("Cannot recalculate set info: card "
                        + card.getSetCode() + "/" + card.getCollectorNumber()
                        + " is missing from the database card catalog");
            }
            totals.addOwned(rarity, count);
        }

        List<UserSetInfo> result = new ArrayList<>(allSetCodes.size());
        for (String setCode : allSetCodes) {
            DatabaseSet databaseSet = databaseSets.get(setCode);
            if (databaseSet.getCardCount() == null || databaseSet.getCardCount() < 0) {
                throw badRequest("Cannot recalculate set info: set " + setCode
                        + " has an invalid total card count");
            }
            SetTotals owned = ownedTotalsBySet.getOrDefault(setCode, new SetTotals());
            RarityTotals rarityTotals = rarityTotalsBySet.getOrDefault(setCode, new RarityTotals());
            long rarityTotal = rarityTotals.totalStandardRarities();
            long ownedRarityTotal = owned.totalStandardRarities();
            long setCardCount = databaseSet.getCardCount();

            UserSetInfo setInfo = new UserSetInfo();
            setInfo.setCardOwnerId(userId);
            setInfo.setSetCode(setCode);
            setInfo.setBaseSetOwned(owned.baseOwned);
            setInfo.setExtraSetOwned(owned.extraOwned);
            setInfo.setFoilsOwned(owned.foilsOwned);
            setInfo.setCommonsCount(owned.commons);
            setInfo.setUncommonsCount(owned.uncommons);
            setInfo.setRaresCount(owned.rares);
            setInfo.setMythicsCount(owned.mythics);
            setInfo.setFullSetBaseOnly(percentage(owned.baseOwned, setCardCount));
            setInfo.setFullSetEverySingle(
                    percentage(addCounts(owned.baseOwned, owned.extraOwned), setCardCount));
            setInfo.setFullSetOneEach(percentage(ownedRarityTotal, rarityTotal));
            result.add(setInfo);
        }
        return result;
    }

    private static long addCounts(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw badRequest("A collection count exceeds the supported range");
        }
    }

    private static Float percentage(long count, long total) {
        if (total == 0) {
            return null;
        }
        return (float) (100.0 * count / total);
    }

    private static OwnedCard toOwnedCard(CollectionBackupCsvParser.CardRow row, Long userId) {
        OwnedCard card = new OwnedCard();
        card.setCardOwnerId(userId);
        card.setSetCode(row.setCode());
        card.setCardName(row.cardName());
        card.setCollectorNumber(row.collectorNumber());
        card.setCardCount(row.count());
        card.setIsFoil(row.foil());
        card.setIsConsideredExtra(row.extra());
        card.setIsTaggedCircle(row.taggedCircle());
        card.setIsTaggedCross(row.taggedCross());
        card.setIsTaggedSquare(row.taggedSquare());
        card.setIsTaggedTriangle(row.taggedTriangle());
        return card;
    }

    private static UserSetInfo toUserSetInfo(CollectionBackupCsvParser.SetRow row, Long userId) {
        UserSetInfo setInfo = new UserSetInfo();
        setInfo.setCardOwnerId(userId);
        setInfo.setSetCode(row.setCode());
        setInfo.setBaseSetOwned(row.baseSetOwned());
        setInfo.setCommonsCount(row.commonsCount());
        setInfo.setExtraSetOwned(row.extraSetOwned());
        setInfo.setFoilsOwned(row.foilsOwned());
        setInfo.setFullSetBaseOnly(row.fullSetBaseOnly());
        setInfo.setFullSetEverySingle(row.fullSetEverySingle());
        setInfo.setFullSetOneEach(row.fullSetOneEach());
        setInfo.setMythicsCount(row.mythicsCount());
        setInfo.setRaresCount(row.raresCount());
        setInfo.setUncommonsCount(row.uncommonsCount());
        return setInfo;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static class SetTotals {
        private long baseOwned;
        private long extraOwned;
        private long foilsOwned;
        private long commons;
        private long uncommons;
        private long rares;
        private long mythics;

        private void addOwned(String rarity, long count) {
            switch (rarity) {
                case "common" -> commons = addCounts(commons, count);
                case "uncommon" -> uncommons = addCounts(uncommons, count);
                case "rare" -> rares = addCounts(rares, count);
                case "mythic" -> mythics = addCounts(mythics, count);
                default -> {
                }
            }
        }

        private long totalStandardRarities() {
            return addCounts(addCounts(commons, uncommons), addCounts(rares, mythics));
        }
    }

    private static class RarityTotals {
        private long commons;
        private long uncommons;
        private long rares;
        private long mythics;

        private void addTotal(String rarity) {
            switch (rarity) {
                case "common" -> commons++;
                case "uncommon" -> uncommons++;
                case "rare" -> rares++;
                case "mythic" -> mythics++;
                default -> {
                }
            }
        }

        private long totalStandardRarities() {
            return commons + uncommons + rares + mythics;
        }
    }

    public record ImportResult(int setsImported, int cardsImported) {
    }
}
