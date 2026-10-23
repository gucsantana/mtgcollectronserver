package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gucsantana.mtgcollectronserver.dto.UserSetInfoPatchRequest;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo.UserSetInfoId;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

class UserSetInfoControllerTest {

    @Test
    void patchingOneFieldPreservesFieldsNotIncludedInRequest() {
        UserSetInfoRepository repository = mock(UserSetInfoRepository.class);
        UserSetInfo existing = existingUserSetInfo();
        UserSetInfoId id = new UserSetInfoId(7L, "tst");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserSetInfoPatchRequest patch = new UserSetInfoPatchRequest();
        patch.setCardOwnerId(7L);
        patch.setSetCode("tst");
        patch.setFoilsOwned(12L);

        UserSetInfo updated = new UserSetInfoController(repository)
                .updateUserSetInfo(patch);

        assertEquals(12L, updated.getFoilsOwned());
        assertEquals(20L, updated.getCommonsCount());
        assertEquals(15L, updated.getUncommonsCount());
        assertEquals(8L, updated.getRaresCount());
        assertEquals(3L, updated.getMythicsCount());
        assertEquals(46L, updated.getBaseSetOwned());
        assertEquals(9L, updated.getExtraSetOwned());
        assertEquals(0.75f, updated.getFullSetEverySingle());
        assertEquals(0.8f, updated.getFullSetBaseOnly());
        assertEquals(0.9f, updated.getFullSetOneEach());
        verify(repository).save(existing);
    }

    @Test
    void patchingExplicitNullClearsOnlyThatField() {
        UserSetInfoRepository repository = mock(UserSetInfoRepository.class);
        UserSetInfo existing = existingUserSetInfo();
        UserSetInfoId id = new UserSetInfoId(7L, "tst");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserSetInfoPatchRequest patch = new UserSetInfoPatchRequest();
        patch.setCardOwnerId(7L);
        patch.setSetCode("tst");
        patch.setCommonsCount(null);

        UserSetInfo updated = new UserSetInfoController(repository)
                .updateUserSetInfo(patch);

        assertNull(updated.getCommonsCount());
        assertEquals(15L, updated.getUncommonsCount());
        assertEquals(4L, updated.getFoilsOwned());
        verify(repository).save(existing);
    }

    private UserSetInfo existingUserSetInfo() {
        UserSetInfo userSetInfo = new UserSetInfo();
        userSetInfo.setCardOwnerId(7L);
        userSetInfo.setSetCode("tst");
        userSetInfo.setCommonsCount(20L);
        userSetInfo.setUncommonsCount(15L);
        userSetInfo.setRaresCount(8L);
        userSetInfo.setMythicsCount(3L);
        userSetInfo.setBaseSetOwned(46L);
        userSetInfo.setExtraSetOwned(9L);
        userSetInfo.setFoilsOwned(4L);
        userSetInfo.setFullSetEverySingle(0.75f);
        userSetInfo.setFullSetBaseOnly(0.8f);
        userSetInfo.setFullSetOneEach(0.9f);
        return userSetInfo;
    }
}
