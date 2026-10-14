package com.gucsantana.mtgcollectronserver.controllers;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.UserPreferences;
import com.gucsantana.mtgcollectronserver.repos.UserPreferencesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user-preferences")
public class UserPreferencesController {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesController(UserPreferencesRepository userPreferencesRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
    }

    @GetMapping
    @Cacheable("user-preferences-cache")
    @Operation(summary = "Returns all user preferences")
    public List<UserPreferences> getAllUserPreferences() {
        return userPreferencesRepository.findAll();
    }

    @GetMapping("/{userId}")
    @Cacheable("user-preferences-cache")
    @Operation(summary = "Returns preferences for a user")
    public UserPreferences getUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return userPreferencesRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User preferences not found for user ID: " + userId));
    }

    @PostMapping
    @CacheEvict(value = "user-preferences-cache", allEntries = true)
    @Operation(summary = "Creates user preferences")
    public UserPreferences createUserPreferences(@Valid @RequestBody UserPreferences userPreferences) {
        return userPreferencesRepository.save(userPreferences);
    }

    @PutMapping("/{userId}")
    @CacheEvict(value = "user-preferences-cache", allEntries = true)
    @Operation(summary = "Updates preferences for a user")
    public UserPreferences updateUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserPreferences userPreferences) {
        if (!userPreferencesRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User preferences not found for user ID: " + userId);
        }

        userPreferences.setUserId(userId);
        return userPreferencesRepository.save(userPreferences);
    }

    @DeleteMapping("/{userId}")
    @CacheEvict(value = "user-preferences-cache", allEntries = true)
    @Operation(summary = "Deletes preferences for a user")
    public void deleteUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userPreferencesRepository.deleteById(userId);
    }
}
