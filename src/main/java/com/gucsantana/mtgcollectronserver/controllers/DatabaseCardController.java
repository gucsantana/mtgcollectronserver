package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;

import java.util.List;

import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @GetMapping("/")
    @Hidden // not visible on Swagger
    public List<DatabaseCard> getAllCards(){
        return dbCardRepo.findAll();
    }

    @GetMapping("/{setCode}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all database cards for a single set")
    public List<DatabaseCard> getAllCardsBySet(@Parameter(description = "Set Code") @PathVariable String setCode){
        return dbCardRepo.findBySetCode(setCode);
    }

    @GetMapping("/{setCode}/{cardName}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all cards of a given name in a single set")
    public List<DatabaseCard> getAllCardsOfNameInSet(@Parameter(description = "Set Code") @PathVariable String setCode, @Parameter(description = "CardName") @PathVariable String cardName){
        return dbCardRepo.findBySetCodeAndCardName(setCode,cardName);
    }

    @GetMapping("/{setCode}/{cardName}/{collectorNumber}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns a specific version of a card in a single set")
    public List<DatabaseCard> getSpecificVersionOfCardInSet(@Parameter(description = "Set Code") @PathVariable String setCode, @Parameter(description = "CardName") @PathVariable String cardName, @Parameter(description = "collectorNumber") @PathVariable String collectorNumber){
        return dbCardRepo.findBySetCodeAndCardNameAndCollectorNumber(setCode,cardName,collectorNumber);
    }

    @GetMapping("/{id}")
    @Transactional
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns a single DatabaseCard by ID")
    public DatabaseCard getUser(@Parameter(description = "Card ID") @PathVariable int id){
        LOGGER.info("Finding database card by Id: "+id);
        return dbCardRepo.findById(id).get();
    }
    
    @PostMapping("/")
    @Operation (summary = "Creates a database card object by passing in a pre-filled DatabaseCard instance")
    public DatabaseCard createUser(@Valid @RequestBody DatabaseCard dbCard){
        LOGGER.info("Created database card: "+ dbCard.getCardName() + " on set " + dbCard.getSetCode() + " with collector number " + dbCard.getCollectorNumber());
        return dbCardRepo.save(dbCard);
    }

    @PutMapping("/")
    @Operation (summary = "Updates a database card object by passing in a pre-filled DatabaseCard instance; uses ID to match")
    public DatabaseCard updateUser(@RequestBody DatabaseCard dbCard){
        LOGGER.info("Updated database card: "+ dbCard.getCardName() + " on set " + dbCard.getSetCode() + " with collector number " + dbCard.getCollectorNumber());
        return dbCardRepo.save(dbCard);
    }

    @DeleteMapping("/{id}")
    @CacheEvict (value = "user-cache")
    @Operation (summary = "Deletes a database card by passing its ID")
    public void deleteUser(@PathVariable int id){
        dbCardRepo.deleteById(id);
    }
}
