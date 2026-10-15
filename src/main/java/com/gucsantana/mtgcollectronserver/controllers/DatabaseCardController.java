package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;

import java.util.List;

import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/card")
@Tag(name="Database Cards REST Endpoint")
public class DatabaseCardController {

    @Autowired 
    DatabaseCardRepository dbCardRepo;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DatabaseCardController.class);

    @GetMapping("/{setCode}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all database cards for a single set")
    public List<DatabaseCard> getAllCardsBySet(@Parameter(description = "Set Code") @PathVariable String set){
        return dbCardRepo.findBySetCode(set);
    }

    @GetMapping("/{setCode}/{cardName}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all cards of a given name in a single set")
    public List<DatabaseCard> getAllCardsOfNameInSet(@Parameter(description = "Set Code") @PathVariable String set, @Parameter(description = "Card Name") @PathVariable String name){
        return dbCardRepo.findBySetCodeAndName(set,name);
    }

    @GetMapping("/{setCode}/{cardName}/{collectorNumber}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns a specific version of a card in a single set")
    public List<DatabaseCard> getSpecificVersionOfCardInSet(@Parameter(description = "Set Code") @PathVariable String set, @Parameter(description = "Card Name") @PathVariable String name, @Parameter(description = "collectorNumber") @PathVariable String collectorNumber){
        return dbCardRepo.findBySetCodeAndNameAndCollectorNumber(set,name,collectorNumber);
    }

    @GetMapping("/id/{id}")
    @Transactional
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns a single DatabaseCard by ID")
    public DatabaseCard getDatabaseCardById(@Parameter(description = "Card ID") @PathVariable String id){
        LOGGER.info("Finding database card by Id: "+id);
        return dbCardRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Database card not found with id: " + id));
    }
    
    @PostMapping("/")
    @CacheEvict(value = "db-card-cache", allEntries = true)
    @Operation (summary = "Creates a database card object by passing in a pre-filled DatabaseCard instance")
    public DatabaseCard createOrUpdateDatabaseCard(@Valid @RequestBody DatabaseCard dbCard){
        if (dbCard.getId() != null) {
            DatabaseCard existingCard = dbCardRepo.findById(dbCard.getId()).orElse(null);
            if (existingCard != null) {
                LOGGER.info("Updating existing database card with ID: " + dbCard.getId());
                existingCard.setSetCode(dbCard.getSetCode());
                existingCard.setName(dbCard.getName());
                existingCard.setCollectorNumber(dbCard.getCollectorNumber());
                existingCard.setRarity(dbCard.getRarity());
                existingCard.setPrices(dbCard.getPrices());
                existingCard.setImageUris(dbCard.getImageUris());
                existingCard.setCardFaces(dbCard.getCardFaces());
                existingCard.setPromoTypes(dbCard.getPromoTypes());
                return dbCardRepo.save(existingCard);
            }
        }

        LOGGER.info("Creating database card: " + dbCard.getName() + " on set " + dbCard.getSetCode()
                + " with collector number " + dbCard.getCollectorNumber());
        return dbCardRepo.save(dbCard);
    }

    @DeleteMapping("/id/{id}")
    @CacheEvict (value = "db-card-cache")
    @Operation (summary = "Deletes a database card by passing its ID")
    public void deleteDatabaseCard(@PathVariable String id){
        dbCardRepo.deleteById(id);
    }
}
