package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.services.ImportBackupJobService;
import com.gucsantana.mtgcollectronserver.services.ImportBackupJobService.ImportJobStatus;

class ImportControllerRequestTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ImportBackupJobService jobs = mock(ImportBackupJobService.class);
    private final ImportController controller = new ImportController(users, passwordEncoder, jobs);

    @Test
    void acceptsValidUploadAndReturnsQueuedJob() {
        User user = new User();
        user.setId(7L);
        user.setPasswordHash("encoded");
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        ImportJobStatus expected = new ImportJobStatus(
                "job-id", "queued", null, null, null, null, null);
        when(jobs.startJob(eq(7L), eq("alice"), any(), eq(true))).thenReturn(expected);

        var response = controller.importBackup(
                new MockMultipartFile("file", "backup.csv", "text/csv", "csv".getBytes()),
                7L, "secret", true, (Principal) () -> "alice");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(jobs).startJob(eq(7L), eq("alice"), any(), eq(true));
    }

    @Test
    void rejectsInvalidPasswordBeforeQueueingJob() {
        User user = new User();
        user.setId(7L);
        user.setPasswordHash("encoded");
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.importBackup(
                        new MockMultipartFile("file", "backup.csv", "text/csv", "csv".getBytes()),
                        7L, "wrong", false, (Principal) () -> "alice"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(jobs, never()).startJob(any(), anyString(), any(), anyBoolean());
    }
}
