package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.dto.UserPreferencesPatchRequest;
import com.gucsantana.mtgcollectronserver.models.UserPreferences;
import com.gucsantana.mtgcollectronserver.repos.UserPreferencesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/user-preferences/{userId}")
public class UserPreferencesController {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesController(UserPreferencesRepository userPreferencesRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
    }

    @GetMapping
    @Cacheable("user-preferences-cache")
    @Operation(summary = "Returns preferences for a user")
    public UserPreferences getUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return userPreferencesRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User preferences not found for user ID: " + userId));
    }

    @PatchMapping
    @CacheEvict(value = "user-preferences-cache", allEntries = true)
    @Operation(summary = "Partially updates preferences for a user; omitted fields are left unchanged")
    public UserPreferences updateUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestBody UserPreferencesPatchRequest patch) {
        UserPreferences userPreferences = userPreferencesRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User preferences not found for user ID: " + userId));

        if (patch.getFullSetOptionSelected() != null) {
            userPreferences.setFullSetOptionSelected(patch.getFullSetOptionSelected());
        }
        if (patch.getCardPerPageOptionSelected() != null) {
            userPreferences.setCardPerPageOptionSelected(patch.getCardPerPageOptionSelected());
        }
        if (patch.getDarkMode() != null) {
            userPreferences.setDarkMode(patch.getDarkMode());
        }
        if (patch.getShowPrices() != null) {
            userPreferences.setShowPrices(patch.getShowPrices());
        }
        if (patch.getShowSidebarPercent() != null) {
            userPreferences.setShowSidebarPercent(patch.getShowSidebarPercent());
        }

        return userPreferencesRepository.save(userPreferences);
    }
}
