package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gucsantana.mtgcollectronserver.dto.OwnedCardPatchRequest;
import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;

class OwnedCardControllerTest {

    @Test
    void patchingFoilPreservesFieldsNotIncludedInRequest() {
        OwnedCardRepository repository = mock(OwnedCardRepository.class);
        OwnedCard existing = new OwnedCard();
        existing.setCardOwnerId(7L);
        existing.setSetCode("tst");
        existing.setCollectorNumber("42");
        existing.setCardName("Example Card");
        existing.setCardCount(3);
        existing.setIsFoil(false);
        existing.setIsConsideredExtra(true);
        existing.setIsTaggedSquare(true);

        when(repository.findByCardOwnerIdAndSetCodeAndCollectorNumber(7L, "tst", "42"))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        OwnedCardPatchRequest patch = new OwnedCardPatchRequest();
        patch.setCardOwnerId(7L);
        patch.setSetCode("tst");
        patch.setCollectorNumber("42");
        patch.setIsFoil(true);

        OwnedCard updated = new OwnedCardController(repository).patchOwnedCard(patch);

        assertEquals(true, updated.getIsFoil());
        assertEquals("Example Card", updated.getCardName());
        assertEquals(3, updated.getCardCount());
        assertEquals(true, updated.getIsConsideredExtra());
        assertEquals(true, updated.getIsTaggedSquare());
        verify(repository).save(existing);
    }
}
