package com.gucsantana.mtgcollectronserver.services;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.services.CollectionBackupImportService.ImportResult;

@Service
public class ImportBackupJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportBackupJobService.class);
    private static final long JOB_RETENTION_SECONDS = 24 * 60 * 60;

    private final CollectionBackupImportService importService;
    private final Executor executor;
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    public ImportBackupJobService(
            CollectionBackupImportService importService,
            @Qualifier("importBackupExecutor") Executor executor) {
        this.importService = importService;
        this.executor = executor;
    }

    public ImportJobStatus startJob(Long userId, String owner, byte[] csvContents, boolean additiveImport) {
        removeExpiredJobs();
        String jobId = UUID.randomUUID().toString();
        Job job = new Job(userId, owner, new ImportJobStatus(jobId, "queued", null, null, null, null, null));
        jobs.put(jobId, job);

        try {
            executor.execute(() -> runJob(jobId, csvContents, additiveImport));
        } catch (RejectedExecutionException exception) {
            jobs.remove(jobId);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Import queue is full; try again later", exception);
        }
        return job.status();
    }

    public ImportJobStatus getStatus(String jobId, String owner) {
        removeExpiredJobs();
        Job job = jobs.get(jobId);
        if (job == null || !job.owner().equals(owner)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Import job not found");
        }
        return job.status();
    }

    private void runJob(String jobId, byte[] csvContents, boolean additiveImport) {
        Job queuedJob = jobs.get(jobId);
        if (queuedJob == null) {
            return;
        }
        Instant startedAt = Instant.now();
        jobs.computeIfPresent(jobId, (ignored, current) -> current.withStatus(
                new ImportJobStatus(jobId, "running", startedAt, null, null, null, null)));

        try {
            ImportResult result = importService.importBackup(csvContents, queuedJob.userId(), additiveImport);
            jobs.computeIfPresent(jobId, (ignored, current) -> current.withStatus(
                    new ImportJobStatus(jobId, "completed", startedAt, Instant.now(),
                            result.setsImported(), result.cardsImported(), null)));
        } catch (RuntimeException exception) {
            LOGGER.error("Collection backup import job {} failed", jobId, exception);
            String error = exception instanceof ResponseStatusException responseException
                    ? responseException.getReason()
                    : null;
            jobs.computeIfPresent(jobId, (ignored, current) -> current.withStatus(
                    new ImportJobStatus(jobId, "failed", startedAt, Instant.now(), null, null,
                            error == null ? "Import failed" : error)));
        }
    }

    private void removeExpiredJobs() {
        Instant cutoff = Instant.now().minusSeconds(JOB_RETENTION_SECONDS);
        jobs.entrySet().removeIf(entry -> {
            ImportJobStatus status = entry.getValue().status();
            return !"queued".equals(status.state())
                    && !"running".equals(status.state())
                    && status.completedAt() != null
                    && status.completedAt().isBefore(cutoff);
        });
    }

    private record Job(Long userId, String owner, ImportJobStatus status) {
        private Job withStatus(ImportJobStatus newStatus) {
            return new Job(userId, owner, newStatus);
        }
    }

    public record ImportJobStatus(
            String jobId,
            String state,
            Instant startedAt,
            Instant completedAt,
            Integer setsImported,
            Integer cardsImported,
            String error) {
    }
}
