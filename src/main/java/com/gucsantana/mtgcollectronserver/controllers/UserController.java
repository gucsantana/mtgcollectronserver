package com.gucsantana.mtgcollectronserver.controllers;

import java.time.LocalDateTime;

import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/{id}")
    @Cacheable("user-cache")
    @Operation(summary = "Returns a user by ID")
    public UserResponse getUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return toUserResponse(findUser(id));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "user-cache", allEntries = true)
    @Operation(summary = "Updates a user account")
    public UserResponse updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody User user) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
        }

        user.setId(id);
        return toUserResponse(userRepository.save(user));
    }

    @PatchMapping("/{id}/active")
    @CacheEvict(value = "user-cache", allEntries = true)
    @Operation(summary = "Activates or deactivates a user account")
    public UserResponse setUserActive(
            @Parameter(description = "User ID") @PathVariable Long id,
            @NotNull @RequestBody Boolean active) {
        User user = findUser(id);
        user.setIsActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        return toUserResponse(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "user-cache", allEntries = true)
    @Operation(summary = "Deletes a user account")
    public void deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
                userRepository.deleteById(id);
                LOGGER.info("Deleted user with id " + id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with ID: " + id));
    }

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getEmailVerifiedAt(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public record UserResponse(
            Long id,
            String email,
            String username,
            LocalDateTime emailVerifiedAt,
            Boolean isActive,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }
}
