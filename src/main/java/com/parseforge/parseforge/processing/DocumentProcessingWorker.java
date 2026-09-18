package com.parseforge.parseforge.processing;

import com.parseforge.parseforge.extractor.DocumentExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
public class DocumentProcessingWorker {

    private final DocumentProcessingQueue processingQueue;
    private final DocumentProcessingJobRepository jobRepository;
    private final DocumentExtractionService extractionService;
    private final int maxAttempts;

    public DocumentProcessingWorker(
            DocumentProcessingQueue processingQueue,
            DocumentProcessingJobRepository jobRepository,
            DocumentExtractionService extractionService,
            @Value("${processing.worker.max-attempts:3}") int maxAttempts
    ) {
        this.processingQueue = processingQueue;
        this.jobRepository = jobRepository;
        this.extractionService = extractionService;
        this.maxAttempts = maxAttempts;
    }

    public void processNextJob() {
        UUID jobId = processingQueue.dequeue();

        if (jobId == null) {
            return;
        }

        DocumentProcessingJob job = jobRepository.findByIdWithDocument(jobId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Processing job not found: " + jobId
                        )
                );

        try {
            OffsetDateTime startedAt = OffsetDateTime.now();

            job.setStatus(ProcessingJobStatus.PROCESSING);
            job.setAttemptCount(job.getAttemptCount() + 1);
            job.setStartedAt(startedAt);
            job.setUpdatedAt(startedAt);

            job.setErrorCode(null);
            job.setErrorMessage(null);

            jobRepository.save(job);

            extractionService.extract(job.getDocument());

            OffsetDateTime completedAt = OffsetDateTime.now();

            job.setStatus(ProcessingJobStatus.COMPLETED);
            job.setCompletedAt(completedAt);
            job.setUpdatedAt(completedAt);

            jobRepository.save(job);
        } catch (DocumentProcessingException e) {
            handleProcessingFailure(job, e);
        } catch (Exception e) {
            log.error(
                    "Document processing failed for jobId={}, documentId={}",
                    job.getId(),
                    job.getDocument().getId(),
                    e
            );

            job.setStatus(ProcessingJobStatus.FAILED);
            job.setErrorCode("PROCESSING_FAILED");
            job.setErrorMessage(e.getMessage());
            job.setUpdatedAt(OffsetDateTime.now());

            jobRepository.save(job);
        }
    }

    private void handleProcessingFailure(
            DocumentProcessingJob job,
            DocumentProcessingException e
    ) {
        job.setErrorCode(e.getErrorCode());
        job.setErrorMessage(e.getMessage());
        job.setUpdatedAt(OffsetDateTime.now());

        if (e.isRetryable() && job.getAttemptCount() < maxAttempts) {
            job.setStatus(ProcessingJobStatus.PENDING);
            jobRepository.save(job);

            processingQueue.enqueue(job.getId());

            log.warn(
                    "Document processing will be retried for jobId={}, attempt={}/{}",
                    job.getId(),
                    job.getAttemptCount(),
                    maxAttempts
            );

            return;
        }

        job.setStatus(ProcessingJobStatus.FAILED);
        jobRepository.save(job);

        log.error(
                "Document processing permanently failed for jobId={}, errorCode={}, attempts={}",
                job.getId(),
                e.getErrorCode(),
                job.getAttemptCount(),
                e
        );
    }
}