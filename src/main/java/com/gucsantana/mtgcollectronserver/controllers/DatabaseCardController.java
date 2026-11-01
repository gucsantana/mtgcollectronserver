package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gucsantana.mtgcollectronserver.dto.CardsByNameInSetRequest;
import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;

import jakarta.validation.Valid;

// import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController 
@RequestMapping ("/card")
@Tag(name="Database Cards REST Endpoint")
public class DatabaseCardController {

    final DatabaseCardRepository dbCardRepo;

    DatabaseCardController(DatabaseCardRepository dbCardRepo) {
        this.dbCardRepo = dbCardRepo;
    }

    // private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DatabaseCardController.class);

    @GetMapping("/{set}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all database cards for a single set")
    public List<DatabaseCard> getAllCardsBySet(@Parameter(description = "Set Code") @PathVariable String set){
        List<DatabaseCard> cards = dbCardRepo.findBySetCode(set);
        cards.sort(Comparator.comparing(
                card -> numericCollectorNumber(card.getCollectorNumber()),
                Comparator.nullsLast(Comparator.naturalOrder())));
        return cards;
    }

    @PostMapping("/byname")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all cards of a given name in a single set")
    public List<DatabaseCard> getAllCardsOfNameInSet(@Valid @RequestBody CardsByNameInSetRequest request){
        List<DatabaseCard> cards = dbCardRepo.findBySetCodeAndName(request.set(), request.name());
        cards.sort(Comparator.comparing(
                card -> numericCollectorNumber(card.getCollectorNumber()),
                Comparator.nullsLast(Comparator.naturalOrder())));
        return cards;
    }

    @GetMapping("/bynumber/{set}/{collectorNumber}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns a specific version of a card in a single set")
    public List<DatabaseCard> getSpecificVersionOfCardInSet(@Parameter(description = "Set Code") @PathVariable String set, @Parameter(description = "collectorNumber") @PathVariable String collectorNumber){
        return dbCardRepo.findBySetCodeAndCollectorNumber(set,collectorNumber);
    }

    // auxiliary method to strip a collector number of non-digits and then convert it to integer
    private static BigInteger numericCollectorNumber(String collectorNumber) {
        if (collectorNumber == null) {
            return null;
        }

        String digits = collectorNumber.replaceAll("\\D", "");
        return digits.isEmpty() ? null : new BigInteger(digits);
    }
}
