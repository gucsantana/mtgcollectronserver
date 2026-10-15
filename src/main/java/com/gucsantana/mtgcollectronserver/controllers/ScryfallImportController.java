package com.gucsantana.mtgcollectronserver.controllers;

import com.gucsantana.mtgcollectronserver.services.ScryfallImportService;
import com.gucsantana.mtgcollectronserver.services.ScryfallImportService.ImportStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/admin/scryfall-import")
public class ScryfallImportController {

    private final ScryfallImportService importService;
    private final String importToken;

    public ScryfallImportController(
            ScryfallImportService importService,
            @Value("${scryfall.import.token}") String importToken) {
        this.importService = importService;
        this.importToken = importToken;
    }

    @PostMapping
    public ResponseEntity<ImportStatus> startImport(
            @RequestHeader(name = "X-Scryfall-Import-Token", required = false) String suppliedToken) {
        authorize(suppliedToken);
        if (!importService.startImport()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A Scryfall import is already running");
        }
        return ResponseEntity.accepted().body(importService.getStatus());
    }

    @GetMapping
    public ImportStatus getStatus(
            @RequestHeader(name = "X-Scryfall-Import-Token", required = false) String suppliedToken) {
        authorize(suppliedToken);
        return importService.getStatus();
    }

    private void authorize(String suppliedToken) {
        if (!StringUtils.hasText(importToken)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Scryfall import is disabled until SCRYFALL_IMPORT_TOKEN is configured");
        }
        if (!StringUtils.hasText(suppliedToken)
                || !MessageDigest.isEqual(
                        importToken.getBytes(StandardCharsets.UTF_8),
                        suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Scryfall import token");
        }
    }
}
