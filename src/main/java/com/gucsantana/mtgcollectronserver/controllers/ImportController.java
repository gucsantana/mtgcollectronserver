package com.gucsantana.mtgcollectronserver.controllers;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.services.ImportBackupJobService;
import com.gucsantana.mtgcollectronserver.services.ImportBackupJobService.ImportJobStatus;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/importbackup")
public class ImportController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImportBackupJobService importBackupJobService;

    public ImportController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ImportBackupJobService importBackupJobService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.importBackupJobService = importBackupJobService;
    }

    @PostMapping
    @Operation(summary = "Queues a CSV collection backup import after verifying the user's password")
    public ResponseEntity<ImportJobStatus> importBackup(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("password") String password,
            @RequestParam(name = "additiveImport", defaultValue = "false") boolean additiveImport,
            Principal principal) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A non-empty backup file is required");
        }

        User user = userRepository.findById(userId)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid user ID or password"));

        byte[] csvContents;
        try {
            csvContents = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read the uploaded backup file", exception);
        }

        ImportJobStatus job = importBackupJobService.startJob(
                user.getId(), principal.getName(), csvContents, additiveImport);
        return ResponseEntity.accepted().body(job);
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Returns the status of a collection backup import job")
    public ImportJobStatus getImportJobStatus(@PathVariable String jobId, Principal principal) {
        return importBackupJobService.getStatus(jobId, principal.getName());
    }
}
