package com.gucsantana.mtgcollectronserver.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

import io.swagger.v3.oas.annotations.Operation;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/importbackup")
public class ImportController {

    private final OwnedCardRepository ownedCardRepository;
    private final UserSetInfoRepository userSetInfoRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public ImportController(
            OwnedCardRepository ownedCardRepository,
            UserSetInfoRepository userSetInfoRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.ownedCardRepository = ownedCardRepository;
        this.userSetInfoRepository = userSetInfoRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @Transactional
    @CacheEvict(value = { "owned-card-cache", "user-set-info-cache" }, allEntries = true)
    @Operation(summary = "Imports a collection backup after verifying the user's password")
    public ImportResult importBackup(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("password") String password) {
        if (file.isEmpty()) {
            throw badRequest("A non-empty backup file is required");
        }

        User user = userRepository.findById(userId)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid user ID or password"));

        JsonNode root;
        try {
            root = objectMapper.readTree(file.getInputStream());
        } catch (JacksonException exception) {
            throw badRequest("The uploaded file is not valid JSON");
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read the uploaded backup file", exception);
        }

        List<OwnedCard> cards = new ArrayList<>();
        List<UserSetInfo> sets = new ArrayList<>();
        validateAndBuildImport(root, user.getId(), cards, sets);

        ownedCardRepository.saveAll(cards);
        userSetInfoRepository.saveAll(sets);
        return new ImportResult(sets.size(), cards.size());
    }

    private static void validateAndBuildImport(
            JsonNode root,
            Long userId,
            List<OwnedCard> cards,
            List<UserSetInfo> sets) {
        if (root == null || !root.isObject() || root.isEmpty()) {
            throw badRequest("The backup must be a non-empty object keyed by set code");
        }

        Iterator<Map.Entry<String, JsonNode>> setEntries = root.properties().iterator();
        while (setEntries.hasNext()) {
            Map.Entry<String, JsonNode> setEntry = setEntries.next();
            String setCode = setEntry.getKey();
            JsonNode setData = setEntry.getValue();
            if (setCode.isBlank() || setCode.length() > 10 || !setData.isObject()) {
                throw badRequest("Invalid set entry: " + setCode);
            }

            JsonNode cardsNode = setData.get("cards");
            if (cardsNode == null || !cardsNode.isObject()) {
                throw badRequest("Set " + setCode + " must contain a cards object");
            }

            sets.add(toUserSetInfo(setData, userId, setCode));
            Iterator<Map.Entry<String, JsonNode>> cardEntries = cardsNode.properties().iterator();
            while (cardEntries.hasNext()) {
                Map.Entry<String, JsonNode> cardEntry = cardEntries.next();
                String cardName = cardEntry.getKey();
                JsonNode versions = cardEntry.getValue();
                if (cardName.isBlank() || cardName.length() > 255 || !versions.isObject()) {
                    throw badRequest("Invalid card entry in set " + setCode + ": " + cardName);
                }

                Iterator<Map.Entry<String, JsonNode>> versionEntries = versions.properties().iterator();
                while (versionEntries.hasNext()) {
                    Map.Entry<String, JsonNode> versionEntry = versionEntries.next();
                    String collectorNumber = versionEntry.getKey();
                    JsonNode details = versionEntry.getValue();
                    if (collectorNumber.isBlank() || collectorNumber.length() > 7 || !details.isObject()) {
                        throw badRequest("Invalid collector number for " + cardName + " in set " + setCode);
                    }
                    cards.add(toOwnedCard(details, userId, setCode, cardName, collectorNumber));
                }
            }
        }
    }

    private static OwnedCard toOwnedCard(
            JsonNode details,
            Long userId,
            String setCode,
            String cardName,
            String collectorNumber) {
        JsonNode count = details.get("count");
        JsonNode foil = details.get("foil");
        JsonNode extra = details.get("extra");
        if (count == null || !count.isIntegralNumber() || !count.canConvertToInt()
                || count.intValue() < 0
                || foil == null || !foil.isBoolean()
                || extra == null || !extra.isBoolean()) {
            throw badRequest("Invalid card details for " + cardName + " (" + setCode + "/" + collectorNumber + ")");
        }

        OwnedCard card = new OwnedCard();
        card.setCardOwnerId(userId);
        card.setSetCode(setCode);
        card.setCardName(cardName);
        card.setCollectorNumber(collectorNumber);
        card.setCardCount(count.intValue());
        card.setIsFoil(foil.booleanValue());
        card.setIsConsideredExtra(extra.booleanValue());
        return card;
    }

    private static UserSetInfo toUserSetInfo(JsonNode setData, Long userId, String setCode) {
        UserSetInfo setInfo = new UserSetInfo();
        setInfo.setCardOwnerId(userId);
        setInfo.setSetCode(setCode);
        setInfo.setCommonsCount(optionalLong(setData, "commons", setCode));
        setInfo.setUncommonsCount(optionalLong(setData, "uncommons", setCode));
        setInfo.setRaresCount(optionalLong(setData, "rares", setCode));
        setInfo.setMythicsCount(optionalLong(setData, "mythics", setCode));
        setInfo.setBaseSetOwned(optionalLong(setData, "base_set_owned", setCode));
        setInfo.setExtraSetOwned(optionalLong(setData, "extra_owned", setCode));
        setInfo.setFoilsOwned(optionalLong(setData, "foils_owned", setCode));
        setInfo.setFullSetEverySingle(optionalFloat(setData, "full_set_every_single", setCode));
        setInfo.setFullSetBaseOnly(optionalFloat(setData, "full_set_base_only", setCode));
        setInfo.setFullSetOneEach(optionalFloat(setData, "full_set_one_each", setCode));
        return setInfo;
    }

    private static Long optionalLong(JsonNode data, String field, String setCode) {
        JsonNode value = data.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isIntegralNumber() || !value.canConvertToLong()) {
            throw badRequest("Invalid " + field + " value for set " + setCode);
        }
        return value.longValue();
    }

    private static Float optionalFloat(JsonNode data, String field, String setCode) {
        JsonNode value = data.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isNumber() || !Double.isFinite(value.doubleValue())
                || !Float.isFinite(value.floatValue())) {
            throw badRequest("Invalid " + field + " value for set " + setCode);
        }
        return value.floatValue();
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public record ImportResult(int setsImported, int cardsImported) {
    }
}
