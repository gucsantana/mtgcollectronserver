package com.gucsantana.mtgcollectronserver.controllers;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.DatabaseSet;
import com.gucsantana.mtgcollectronserver.repos.DatabaseSetRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/sets")
public class DatabaseSetController {

    private final DatabaseSetRepository databaseSetRepository;

    public DatabaseSetController(DatabaseSetRepository databaseSetRepository) {
        this.databaseSetRepository = databaseSetRepository;
    }

    @GetMapping
    @Cacheable("db-set-cache")
    @Operation(summary = "Returns all set information from database")
    public List<DatabaseSet> getAllSets() {
        return databaseSetRepository.findAllByOrderByReleasedAtDesc();
    }

    @GetMapping("/{setCode}")
    @Cacheable("db-set-cache")
    @Operation(summary = "Returns a database set by set code")
    public DatabaseSet getSet(@Parameter(description = "Set code") @PathVariable String setCode) {
        return databaseSetRepository.findById(setCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Database set not found with code: " + setCode));
    }
}
