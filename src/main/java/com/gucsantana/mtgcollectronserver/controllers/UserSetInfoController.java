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

import com.gucsantana.mtgcollectronserver.models.UserSetInfo;
import com.gucsantana.mtgcollectronserver.models.UserSetInfo.UserSetInfoId;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user-set-info")
public class UserSetInfoController {

    private final UserSetInfoRepository userSetInfoRepository;

    public UserSetInfoController(UserSetInfoRepository userSetInfoRepository) {
        this.userSetInfoRepository = userSetInfoRepository;
    }

    @GetMapping("/{cardOwnerId}")
    @Cacheable("user-set-info-cache")
    @Operation(summary = "Returns all set information for a user")
    public List<UserSetInfo> getAllUserSetInfoForUser(
            @Parameter(description = "User ID") @PathVariable Long cardOwnerId) {
        return userSetInfoRepository.findByCardOwnerId(cardOwnerId);
    }

    @GetMapping("/{cardOwnerId}/{setCode}")
    @Cacheable("user-set-info-cache")
    @Operation(summary = "Returns set information for a user and set")
    public UserSetInfo getUserSetInfo(
            @Parameter(description = "User ID") @PathVariable Long cardOwnerId,
            @Parameter(description = "Set code") @PathVariable String setCode) {
        return userSetInfoRepository.findById(new UserSetInfoId(cardOwnerId, setCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Set information not found for user ID " + cardOwnerId + " and set " + setCode));
    }

    @PostMapping
    @CacheEvict(value = "user-set-info-cache", allEntries = true)
    @Operation(summary = "Creates user set information")
    public UserSetInfo createUserSetInfo(@Valid @RequestBody UserSetInfo userSetInfo) {
        return userSetInfoRepository.save(userSetInfo);
    }

    @PutMapping("/{cardOwnerId}/{setCode}")
    @CacheEvict(value = "user-set-info-cache", allEntries = true)
    @Operation(summary = "Updates set information for a user and set")
    public UserSetInfo updateUserSetInfo(
            @Parameter(description = "User ID") @PathVariable Long cardOwnerId,
            @Parameter(description = "Set code") @PathVariable String setCode,
            @Valid @RequestBody UserSetInfo userSetInfo) {
        UserSetInfoId id = new UserSetInfoId(cardOwnerId, setCode);
        if (!userSetInfoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Set information not found for user ID " + cardOwnerId + " and set " + setCode);
        }

        userSetInfo.setCardOwnerId(cardOwnerId);
        userSetInfo.setSetCode(setCode);
        return userSetInfoRepository.save(userSetInfo);
    }

    @DeleteMapping("/{cardOwnerId}/{setCode}")
    @CacheEvict(value = "user-set-info-cache", allEntries = true)
    @Operation(summary = "Deletes set information for a user and set")
    public void deleteUserSetInfo(
            @Parameter(description = "User ID") @PathVariable Long cardOwnerId,
            @Parameter(description = "Set code") @PathVariable String setCode) {
        userSetInfoRepository.deleteById(new UserSetInfoId(cardOwnerId, setCode));
    }
}
