package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;
import tools.jackson.databind.ObjectMapper;

class ImportControllerTest {

    @Test
    void importsCardsAndSetInfoAfterVerifyingPassword() {
        OwnedCardRepository cards = mock(OwnedCardRepository.class);
        UserSetInfoRepository sets = mock(UserSetInfoRepository.class);
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User user = new User();
        user.setId(7L);
        user.setPasswordHash("encoded-password");
        when(users.findById(7L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);

        ImportController controller = new ImportController(
                cards, sets, users, passwordEncoder, new ObjectMapper());
        MockMultipartFile file = backupFile("""
                {
                  "tst": {
                    "cards": {
                      "Example Card": {
                        "42": {"count": 3, "foil": true, "extra": false}
                      }
                    },
                    "commons": 1,
                    "uncommons": 2,
                    "rares": 3,
                    "mythics": 0,
                    "base_set_owned": 4,
                    "extra_owned": 1,
                    "foils_owned": 2,
                    "full_set_every_single": 4.5,
                    "full_set_base_only": 3.5,
                    "full_set_one_each": 5.5
                  }
                }
                """);

        ImportController.ImportResult result =
                controller.importBackup(file, 7L, "correct-password");

        assertEquals(new ImportController.ImportResult(1, 1), result);
        verify(cards).saveAll(argThat(imported -> {
            OwnedCard card = imported.iterator().next();
            return card.getCardOwnerId().equals(7L)
                    && card.getSetCode().equals("tst")
                    && card.getCardName().equals("Example Card")
                    && card.getCollectorNumber().equals("42")
                    && card.getCardCount().equals(3)
                    && card.getIsFoil()
                    && !card.getIsTaggedSquare()
                    && !card.getIsTaggedCircle()
                    && !card.getIsTaggedTriangle()
                    && !card.getIsTaggedCross()
                    && !card.getIsConsideredExtra();
        }));
        verify(sets).saveAll(argThat(imported -> {
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
    void importsCardNamesLongerThanThePreviousColumnLimit() {
        OwnedCardRepository cards = mock(OwnedCardRepository.class);
        UserSetInfoRepository sets = mock(UserSetInfoRepository.class);
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User user = new User();
        user.setId(7L);
        user.setPasswordHash("encoded-password");
        when(users.findById(7L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);

        String longCardName = "x".repeat(141);
        String backup = "{\"tst\":{\"cards\":{\"" + longCardName
                + "\":{\"42\":{\"count\":1,\"foil\":false,\"extra\":false}}}}}";
        ImportController controller = new ImportController(
                cards, sets, users, passwordEncoder, new ObjectMapper());

        ImportController.ImportResult result =
                controller.importBackup(backupFile(backup), 7L, "correct-password");

        assertEquals(new ImportController.ImportResult(1, 1), result);
        verify(cards).saveAll(argThat(imported ->
                imported.iterator().next().getCardName().equals(longCardName)));
    }

    @Test
    void rejectsInvalidPasswordWithoutWritingData() {
        OwnedCardRepository cards = mock(OwnedCardRepository.class);
        UserSetInfoRepository sets = mock(UserSetInfoRepository.class);
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User user = new User();
        user.setId(7L);
        user.setPasswordHash("encoded-password");
        when(users.findById(7L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ImportController controller = new ImportController(
                cards, sets, users, passwordEncoder, new ObjectMapper());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.importBackup(backupFile("{}"), 7L, "incorrect-password"));

        assertEquals(401, exception.getStatusCode().value());
        verify(cards, never()).saveAll(any());
        verify(sets, never()).saveAll(any());
    }

    @Test
    void rejectsInvalidCardDataBeforeWritingData() {
        OwnedCardRepository cards = mock(OwnedCardRepository.class);
        UserSetInfoRepository sets = mock(UserSetInfoRepository.class);
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User user = new User();
        user.setId(7L);
        user.setPasswordHash("encoded-password");
        when(users.findById(7L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);

        ImportController controller = new ImportController(
                cards, sets, users, passwordEncoder, new ObjectMapper());
        MockMultipartFile file = backupFile("""
                {"tst":{"cards":{"Example Card":{"42":{"count":"three","foil":false,"extra":false}}}}}
                """);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.importBackup(file, 7L, "correct-password"));

        assertEquals(400, exception.getStatusCode().value());
        verify(cards, never()).saveAll(any());
        verify(sets, never()).saveAll(any());
    }

    private static MockMultipartFile backupFile(String contents) {
        return new MockMultipartFile("file", "backup.json", "application/json",
                contents.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
