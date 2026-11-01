package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.dto.OwnedCardDeleteRequest;
import com.gucsantana.mtgcollectronserver.dto.OwnedCardByNameSearchRequest;
import com.gucsantana.mtgcollectronserver.dto.OwnedCardPatchRequest;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/byname")
    @Cacheable(value = "owned-card-cache")
    @Operation(summary = "Returns all owned cards of a single user matching the given name")
    public List<OwnedCard> getAllCardsOfUserInSetWithName(
            @Valid @RequestBody OwnedCardByNameSearchRequest request) {
        return ownCardRepo.findByCardOwnerIdAndCardName(request.cardOwnerId(), request.cardName());
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

    @PostMapping("/card-finder")
    @Cacheable(value = "owned-card-cache")
    @Operation(summary = "Returns all owned cards for a single user that partially match the card name provided")
    public List<OwnedCard> getAllCardsOfUserMatchingCardName(
            @Valid @RequestBody OwnedCardByNameSearchRequest request) {
        return ownCardRepo.findByCardOwnerIdAndCardNameContainingIgnoreCase(
                request.cardOwnerId(), request.cardName());
    }

    // -------------------------------------------------------------------------------------------

    @PostMapping("/")
    @CacheEvict(value = "owned-card-cache", allEntries = true)
    @Operation(summary = "Creates a database card object by passing in a pre-filled OwnedCard instance")
    public OwnedCard createOwnedCard(@Valid @RequestBody OwnedCard ownCard) {
        LOGGER.info("Created database card: " + ownCard.getCardName() + " on set " + ownCard.getSetCode()
                + " with collector number " + ownCard.getCollectorNumber() + " for user " + ownCard.getCardOwnerId());
        return ownCardRepo.save(ownCard);
    }

    @PatchMapping("/")
    @CacheEvict(value = "owned-card-cache", allEntries = true)
    @Operation(summary = "Partially updates an owned card; omitted fields are left unchanged")
    public OwnedCard patchOwnedCard(@Valid @RequestBody OwnedCardPatchRequest patch) {
        OwnedCard ownCard = ownCardRepo.findByCardOwnerIdAndSetCodeAndCollectorNumber(
                patch.getCardOwnerId(), patch.getSetCode(), patch.getCollectorNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owned card not found"));

        if (patch.hasCardName()) {
            if (patch.getCardName() == null || patch.getCardName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "card_name cannot be null or blank");
            }
            ownCard.setCardName(patch.getCardName());
        }
        if (patch.hasCardCount()) {
            ownCard.setCardCount(patch.getCardCount());
        }
        if (patch.hasIsFoil()) {
            if (patch.getIsFoil() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "is_foil cannot be null");
            }
            ownCard.setIsFoil(patch.getIsFoil());
        }
        if (patch.hasIsConsideredExtra()) {
            ownCard.setIsConsideredExtra(patch.getIsConsideredExtra());
        }
        if (patch.hasIsTaggedSquare()) {
            ownCard.setIsTaggedSquare(patch.getIsTaggedSquare());
        }
        if (patch.hasIsTaggedCircle()) {
            ownCard.setIsTaggedCircle(patch.getIsTaggedCircle());
        }
        if (patch.hasIsTaggedTriangle()) {
            ownCard.setIsTaggedTriangle(patch.getIsTaggedTriangle());
        }
        if (patch.hasIsTaggedCross()) {
            ownCard.setIsTaggedCross(patch.getIsTaggedCross());
        }

        return ownCardRepo.save(ownCard);
    }

    @DeleteMapping("/")
    @CacheEvict(value = "owned-card-cache", allEntries = true)
    @Operation(summary = "Deletes a database card by passing its owner's id, set code, and collector number")
    public void deleteOwnedCard(@Valid @RequestBody OwnedCardDeleteRequest request) {
        ownCardRepo.deleteByCardOwnerIdAndSetCodeAndCollectorNumber(
                request.getCardOwnerId(), request.getSetCode(), request.getCollectorNumber());
    }
}
