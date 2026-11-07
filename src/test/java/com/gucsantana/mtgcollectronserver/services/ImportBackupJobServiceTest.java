package com.gucsantana.mtgcollectronserver.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.services.CollectionBackupImportService.ImportResult;
import com.gucsantana.mtgcollectronserver.services.ImportBackupJobService.ImportJobStatus;

class ImportBackupJobServiceTest {

    @Test
    void queuesJobAndReportsCompletionToItsOwner() {
        CollectionBackupImportService importService = mock(CollectionBackupImportService.class);
        List<Runnable> queuedTasks = new ArrayList<>();
        ImportBackupJobService jobService = new ImportBackupJobService(importService, queuedTasks::add);
        byte[] csvContents = "backup".getBytes();
        when(importService.importBackup(csvContents, 7L, false)).thenReturn(new ImportResult(2, 5));

        ImportJobStatus accepted = jobService.startJob(7L, "alice", csvContents, false);

        assertEquals("queued", accepted.state());
        assertEquals("queued", jobService.getStatus(accepted.jobId(), "alice").state());
        queuedTasks.getFirst().run();
        ImportJobStatus completed = jobService.getStatus(accepted.jobId(), "alice");
        assertEquals("completed", completed.state());
        assertEquals(2, completed.setsImported());
        assertEquals(5, completed.cardsImported());
        verify(importService).importBackup(csvContents, 7L, false);
    }

    @Test
    void hidesJobsFromOtherUsers() {
        ImportBackupJobService jobService = new ImportBackupJobService(
                mock(CollectionBackupImportService.class), command -> {
                });
        ImportJobStatus accepted = jobService.startJob(7L, "alice", new byte[] { 1 }, false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> jobService.getStatus(accepted.jobId(), "bob"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void reportsImportFailuresAsTerminalJobState() {
        CollectionBackupImportService importService = mock(CollectionBackupImportService.class);
        List<Runnable> queuedTasks = new ArrayList<>();
        ImportBackupJobService jobService = new ImportBackupJobService(importService, queuedTasks::add);
        byte[] csvContents = "backup".getBytes();
        when(importService.importBackup(csvContents, 7L, false))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV"));

        ImportJobStatus accepted = jobService.startJob(7L, "alice", csvContents, false);
        queuedTasks.getFirst().run();

        ImportJobStatus failed = jobService.getStatus(accepted.jobId(), "alice");
        assertEquals("failed", failed.state());
        assertEquals("Invalid CSV", failed.error());
    }
}
