package com.gucsantana.mtgcollectronserver.controllers;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.dto.UserSetInfoPatchRequest;
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

    @PostMapping("/")
    @CacheEvict(value = "user-set-info-cache", allEntries = true)
    @Operation(summary = "Creates user set information")
    public UserSetInfo createUserSetInfo(@Valid @RequestBody UserSetInfo userSetInfo) {
        return userSetInfoRepository.save(userSetInfo);
    }

    @PatchMapping("/")
    @CacheEvict(value = "user-set-info-cache", allEntries = true)
    @Operation(summary = "Partially updates set information for a user and set")
    public UserSetInfo updateUserSetInfo(@Valid @RequestBody UserSetInfoPatchRequest patch) {
        Long cardOwnerId = patch.getCardOwnerId();
        String setCode = patch.getSetCode();
        UserSetInfoId id = new UserSetInfoId(cardOwnerId, setCode);
        UserSetInfo userSetInfo = userSetInfoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Set information not found for user ID " + cardOwnerId + " and set " + setCode));

        if (patch.hasCommonsCount()) {
            userSetInfo.setCommonsCount(patch.getCommonsCount());
        }
        if (patch.hasUncommonsCount()) {
            userSetInfo.setUncommonsCount(patch.getUncommonsCount());
        }
        if (patch.hasRaresCount()) {
            userSetInfo.setRaresCount(patch.getRaresCount());
        }
        if (patch.hasMythicsCount()) {
            userSetInfo.setMythicsCount(patch.getMythicsCount());
        }
        if (patch.hasBaseSetOwned()) {
            userSetInfo.setBaseSetOwned(patch.getBaseSetOwned());
        }
        if (patch.hasExtraSetOwned()) {
            userSetInfo.setExtraSetOwned(patch.getExtraSetOwned());
        }
        if (patch.hasFoilsOwned()) {
            userSetInfo.setFoilsOwned(patch.getFoilsOwned());
        }
        if (patch.hasFullSetEverySingle()) {
            userSetInfo.setFullSetEverySingle(patch.getFullSetEverySingle());
        }
        if (patch.hasFullSetBaseOnly()) {
            userSetInfo.setFullSetBaseOnly(patch.getFullSetBaseOnly());
        }
        if (patch.hasFullSetOneEach()) {
            userSetInfo.setFullSetOneEach(patch.getFullSetOneEach());
        }

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
