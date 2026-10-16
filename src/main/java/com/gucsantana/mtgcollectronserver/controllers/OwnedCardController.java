package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;

import java.util.List;

import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/owncard")
@Tag(name = "Owned Cards REST Endpoint")
public class OwnedCardController {

    final OwnedCardRepository ownCardRepo;

    OwnedCardController(OwnedCardRepository ownCardRepo) {
        this.ownCardRepo = ownCardRepo;
    }
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OwnedCardController.class);

    @GetMapping("/{id}")
    @Cacheable(value = "owned-card-cache")
    @Operation(summary = "Returns all owned cards for a single user")
    public List<OwnedCard> getAllCardsOfUser(@Parameter(description = "User ID") @PathVariable Long id) {
        return ownCardRepo.findByCardOwnerId(id);
    }

    @GetMapping("/{id}/{setCode}")
    @Cacheable(value = "owned-card-cache")
    @Operation(summary = "Returns all owned cards of a single set for a single user")
    public List<OwnedCard> getAllCardsOfUserInSet(@Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Set Code") @PathVariable String setCode) {
        return ownCardRepo.findByCardOwnerIdAndSetCode(id, setCode);
    }

    @GetMapping("/{id}/{setCode}/{cardName}")
    @Cacheable(value = "owned-card-cache")
    @Operation(summary = "Returns all owned cards of a single set for a single user matching the given name")
    public List<OwnedCard> getAllCardsOfUserInSetWithName(@Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Set Code") @PathVariable String setCode,
            @Parameter(description = "CardName") @PathVariable String cardName) {
        return ownCardRepo.findByCardOwnerIdAndSetCodeAndCardName(id, setCode, cardName);
    }

    @GetMapping("/{id}/{setCode}/{cardName}/{collectorNumber}")
    @Cacheable(value = "owned-card-cache")
    @Operation(summary = "Returns a specific card for a specific user, matching set, card name, and collector number")
    public List<OwnedCard> getSpecificVersionOfCardInSetForUser(@Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Set Code") @PathVariable String setCode,
            @Parameter(description = "Card Name") @PathVariable String cardName,
            @Parameter(description = "Collector Number") @PathVariable String collectorNumber) {
        return ownCardRepo.findByCardOwnerIdAndSetCodeAndCardNameAndCollectorNumber(id, setCode, cardName, collectorNumber);
    }

    @PostMapping("/")
    @CacheEvict(value = "owned-card-cache", allEntries = true)
    @Operation(summary = "Creates a database card object by passing in a pre-filled OwnedCard instance")
    public OwnedCard createOwnedCard(@Valid @RequestBody OwnedCard ownCard) {
        LOGGER.info("Created database card: " + ownCard.getCardName() + " on set " + ownCard.getSetCode()
                + " with collector number " + ownCard.getCollectorNumber() + " for user " + ownCard.getCardOwnerId());
        return ownCardRepo.save(ownCard);
    }

    @PutMapping("/")
    @CacheEvict(value = "owned-card-cache", allEntries = true)
    @Operation(summary = "Updates a database card object by passing in a pre-filled OwnedCard instance; uses ID to match")
    public OwnedCard updateOwnedCard(@Valid @RequestBody OwnedCard ownCard) {
        LOGGER.info("Updated database card: " + ownCard.getCardName() + " on set " + ownCard.getSetCode()
                + " with collector number " + ownCard.getCollectorNumber() + " for user " + ownCard.getCardOwnerId());
        return ownCardRepo.save(ownCard);
    }

    @DeleteMapping("/{id}/{setCode}/{cardName}/{collectorNumber}")
    @CacheEvict(value = "owned-card-cache", allEntries = true)
    @Operation(summary = "Deletes a database card by passing its owner's id, set code, card name, and collector number")
    public void deleteOwnedCard(@Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Set Code") @PathVariable String setCode,
            @Parameter(description = "Card Name") @PathVariable String cardName,
            @Parameter(description = "Collector Number") @PathVariable String collectorNumber) {
        ownCardRepo.deleteByCardOwnerIdAndSetCodeAndCardNameAndCollectorNumber(id, setCode, cardName, collectorNumber);
    }
}
