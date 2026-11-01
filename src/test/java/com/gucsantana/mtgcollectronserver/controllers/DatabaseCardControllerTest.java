package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gucsantana.mtgcollectronserver.dto.CardsByNameInSetRequest;
import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;

class DatabaseCardControllerTest {

    @Test
    void findsCardsByNameAndSetAndSortsByNumericCollectorNumber() {
        DatabaseCardRepository repository = mock(DatabaseCardRepository.class);
        DatabaseCard card10 = cardWithCollectorNumber("10");
        DatabaseCard card2 = cardWithCollectorNumber("2");
        when(repository.findBySetCodeAndName("tst", "Example Card"))
                .thenReturn(new ArrayList<>(List.of(card10, card2)));

        List<DatabaseCard> cards = new DatabaseCardController(repository)
                .getAllCardsOfNameInSet(new CardsByNameInSetRequest("tst", "Example Card"));

        assertEquals(List.of(card2, card10), cards);
        verify(repository).findBySetCodeAndName("tst", "Example Card");
    }

    private static DatabaseCard cardWithCollectorNumber(String collectorNumber) {
        DatabaseCard card = new DatabaseCard();
        card.setCollectorNumber(collectorNumber);
        return card;
    }
}
