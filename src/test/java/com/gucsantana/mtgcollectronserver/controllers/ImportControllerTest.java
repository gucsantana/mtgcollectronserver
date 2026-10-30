package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.models.DatabaseSet;
import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;
import com.gucsantana.mtgcollectronserver.repos.DatabaseSetRepository;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

class ImportControllerTest {

    @Test
    void replacesUserCollectionWithCsvAfterVerifyingPassword() {
        TestContext context = new TestContext();
        ImportController.ImportResult result = context.controller.importBackup(
                backupFile("\uFEFF" + validCsv("Name, with comma", "3", "true")),
                7L, "correct-password", false);

        assertEquals(new ImportController.ImportResult(1, 1), result);
        verify(context.cards).deleteByCardOwnerId(7L);
        verify(context.sets).deleteByCardOwnerId(7L);
        verify(context.cards).saveAll(argThat(imported -> {
            OwnedCard card = imported.iterator().next();
            return card.getCardOwnerId().equals(7L)
                    && card.getSetCode().equals("tst")
                    && card.getCardName().equals("Name, with comma")
                    && card.getCollectorNumber().equals("42")
                    && card.getCardCount().equals(3)
                    && card.getIsFoil()
                    && card.getIsTaggedCircle()
                    && !card.getIsTaggedSquare()
                    && !card.getIsTaggedTriangle()
                    && !card.getIsTaggedCross()
                    && !card.getIsConsideredExtra();
        }));
        verify(context.sets).saveAll(argThat(imported -> {
            UserSetInfo set = imported.iterator().next();
            return set.getCardOwnerId().equals(7L)
                    && set.getSetCode().equals("tst")
                    && set.getCommonsCount().equals(1L)
                    && set.getExtraSetOwned().equals(1L)
                    && set.getFoilsOwned().equals(2L)
                    && set.getFullSetEverySingle().equals(4.5f);
        }));
    }

    @Test
    void additiveImportMergesCountsReplacesFlagsAndRecalculatesSetInfo() {
        TestContext context = new TestContext();
        OwnedCard existing = ownedCard(2, false);
        when(context.cards.findByCardOwnerId(7L)).thenReturn(List.of(existing));
        when(context.sets.findByCardOwnerId(7L)).thenReturn(List.of());
        when(context.databaseSets.findAllById(any())).thenReturn(List.of(databaseSet("tst", 10)));
        when(context.databaseCards.findBySetCodeIn(anyCollection()))
                .thenReturn(List.of(databaseCard("tst", "42", "common")));

        ImportController.ImportResult result = context.controller.importBackup(
                backupFile(validCsv("Example Card", "3", "true")), 7L, "correct-password", true);

        assertEquals(new ImportController.ImportResult(1, 1), result);
        verify(context.cards, never()).deleteByCardOwnerId(any());
        verify(context.sets, never()).deleteByCardOwnerId(any());
        verify(context.cards).saveAll(argThat(imported -> {
            OwnedCard merged = imported.iterator().next();
            return merged.getCardCount().equals(5)
                    && merged.getIsFoil()
                    && merged.getCardName().equals("Example Card");
        }));
        verify(context.sets).saveAll(argThat(imported -> {
            UserSetInfo set = imported.iterator().next();
            return set.getBaseSetOwned().equals(5L)
                    && set.getExtraSetOwned().equals(0L)
                    && set.getFoilsOwned().equals(5L)
                    && set.getCommonsCount().equals(5L)
                    && set.getFullSetBaseOnly().equals(50.0f)
                    && set.getFullSetEverySingle().equals(50.0f)
                    && set.getFullSetOneEach().equals(500.0f);
        }));
    }

    @Test
    void rejectsMissingRequiredCsvHeaderFieldsBeforeDeletingCollection() {
        TestContext context = new TestContext();
        String invalidCsv = """
                "card_info"
                "card_name","set_code","card_count","collector_number","is_considered_extra","is_foil","is_tagged_circle","is_tagged_square","is_tagged_triangle"
                "Example Card","tst","1","42","false","false","false","false","false"
                "set_info"
                "set_code","base_set_owned","commons_count","extra_set_owned","foils_owned","full_set_base_only","full_set_every_single","full_set_one_each","mythics_count","rares_count","uncommons_count"
                "tst","1","1","0","0","10","10","10","0","0","0"
                """;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> context.controller.importBackup(
                        backupFile(invalidCsv), 7L, "correct-password", false));

        assertEquals(400, exception.getStatusCode().value());
        verify(context.cards, never()).deleteByCardOwnerId(any());
        verify(context.sets, never()).deleteByCardOwnerId(any());
        verify(context.cards, never()).saveAll(any());
        verify(context.sets, never()).saveAll(any());
    }

    @Test
    void rejectsInvalidPasswordWithoutWritingData() {
        TestContext context = new TestContext();
        when(context.passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> context.controller.importBackup(
                        backupFile(validCsv("Example Card", "1", "false")),
                        7L, "incorrect-password", false));

        assertEquals(401, exception.getStatusCode().value());
        verify(context.cards, never()).deleteByCardOwnerId(any());
        verify(context.sets, never()).deleteByCardOwnerId(any());
        verify(context.cards, never()).saveAll(any());
        verify(context.sets, never()).saveAll(any());
    }

    private static OwnedCard ownedCard(int count, boolean foil) {
        OwnedCard card = new OwnedCard();
        card.setCardOwnerId(7L);
        card.setSetCode("tst");
        card.setCardName("Old Name");
        card.setCollectorNumber("42");
        card.setCardCount(count);
        card.setIsFoil(foil);
        card.setIsConsideredExtra(false);
        return card;
    }

    private static DatabaseSet databaseSet(String code, int cardCount) {
        DatabaseSet set = new DatabaseSet();
        set.setCode(code);
        set.setCardCount(cardCount);
        return set;
    }

    private static DatabaseCard databaseCard(String setCode, String collectorNumber, String rarity) {
        DatabaseCard card = new DatabaseCard();
        card.setSetCode(setCode);
        card.setCollectorNumber(collectorNumber);
        card.setRarity(rarity);
        return card;
    }

    private static String validCsv(String cardName, String cardCount, String foil) {
        return """
                "card_info"
                "card_name","set_code","card_count","collector_number","is_considered_extra","is_foil","is_tagged_circle","is_tagged_cross","is_tagged_square","is_tagged_triangle"
                "%s","tst","%s","42","false","%s","true","false","false","false"
                "set_info"
                "set_code","base_set_owned","commons_count","extra_set_owned","foils_owned","full_set_base_only","full_set_every_single","full_set_one_each","mythics_count","rares_count","uncommons_count"
                "tst","1","1","1","2","3.5","4.5","5.5","0","0","0"
                """.formatted(cardName, cardCount, foil);
    }

    private static MockMultipartFile backupFile(String contents) {
        return new MockMultipartFile("file", "backup.csv", "text/csv",
                contents.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static class TestContext {
        private final OwnedCardRepository cards = mock(OwnedCardRepository.class);
        private final UserSetInfoRepository sets = mock(UserSetInfoRepository.class);
        private final UserRepository users = mock(UserRepository.class);
        private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        private final DatabaseSetRepository databaseSets = mock(DatabaseSetRepository.class);
        private final DatabaseCardRepository databaseCards = mock(DatabaseCardRepository.class);
        private final ImportController controller;

        private TestContext() {
            User user = new User();
            user.setId(7L);
            user.setPasswordHash("encoded-password");
            when(users.findById(7L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);
            controller = new ImportController(
                    cards, sets, users, passwordEncoder, databaseSets, databaseCards);
        }
    }
}
