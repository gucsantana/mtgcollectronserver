package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gucsantana.mtgcollectronserver.dto.UserPreferencesPatchRequest;
import com.gucsantana.mtgcollectronserver.models.UserPreferences;
import com.gucsantana.mtgcollectronserver.repos.UserPreferencesRepository;

class UserPreferencesControllerTest {

    @Test
    void updatesOnlyNonNullPreferences() {
        UserPreferencesRepository repository = mock(UserPreferencesRepository.class);
        UserPreferences existing = new UserPreferences();
        existing.setUserId(42L);
        existing.setFullSetOptionSelected(1);
        existing.setCardPerPageOptionSelected(60);
        existing.setDarkMode(true);
        existing.setShowPrices(true);
        existing.setShowSidebarPercent(false);
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.save(any(UserPreferences.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferencesPatchRequest patch = new UserPreferencesPatchRequest();
        patch.setCardPerPageOptionSelected(120);
        patch.setDarkMode(false);
        patch.setShowSidebarPercent(true);

        UserPreferencesController controller = new UserPreferencesController(repository);
        UserPreferences result = controller.updateUserPreferences(42L, patch);

        assertEquals(1, result.getFullSetOptionSelected());
        assertEquals(120, result.getCardPerPageOptionSelected());
        assertEquals(false, result.getDarkMode());
        assertEquals(true, result.getShowPrices());
        assertEquals(true, result.getShowSidebarPercent());
        verify(repository).save(argThat(preferences ->
                preferences.getUserId().equals(42L)
                        && preferences.getFullSetOptionSelected().equals(1)
                        && preferences.getCardPerPageOptionSelected().equals(120)
                        && Boolean.FALSE.equals(preferences.getDarkMode())
                        && Boolean.TRUE.equals(preferences.getShowPrices())
                        && Boolean.TRUE.equals(preferences.getShowSidebarPercent())));
    }
}
