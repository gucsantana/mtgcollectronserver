package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;
import com.gucsantana.mtgcollectronserver.repos.DatabaseCardRepository;

import java.util.List;

// import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController 
@RequestMapping ("/card")
@Tag(name="Database Cards REST Endpoint")
public class DatabaseCardController {

    @Autowired 
    DatabaseCardRepository dbCardRepo;

    // private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DatabaseCardController.class);

    @GetMapping("/{set}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all database cards for a single set")
    public List<DatabaseCard> getAllCardsBySet(@Parameter(description = "Set Code") @PathVariable String set){
        return dbCardRepo.findBySetCodeOrderByCollectorNumberAsc(set);
    }

    @GetMapping("/{set}/{name}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns all cards of a given name in a single set")
    public List<DatabaseCard> getAllCardsOfNameInSet(@Parameter(description = "Set Code") @PathVariable String set, @Parameter(description = "Card Name") @PathVariable String name){
        return dbCardRepo.findBySetCodeAndNameOrderByCollectorNumberAsc(set,name);
    }

    @GetMapping("/{set}/{name}/{collectorNumber}")
    @Cacheable (value = "db-card-cache")
    @Operation (summary = "Returns a specific version of a card in a single set")
    public List<DatabaseCard> getSpecificVersionOfCardInSet(@Parameter(description = "Set Code") @PathVariable String set, @Parameter(description = "Card Name") @PathVariable String name, @Parameter(description = "collectorNumber") @PathVariable String collectorNumber){
        return dbCardRepo.findBySetCodeAndNameAndCollectorNumberOrderByCollectorNumberAsc(set,name,collectorNumber);
    }
}
