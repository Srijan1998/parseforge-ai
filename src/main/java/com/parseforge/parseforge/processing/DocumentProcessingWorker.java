package com.parseforge.parseforge.processing;

import com.parseforge.parseforge.extractor.DocumentExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
public class DocumentProcessingWorker {

    private final DocumentProcessingQueue processingQueue;
    private final DocumentProcessingJobRepository jobRepository;
    private final DocumentExtractionService extractionService;

    public DocumentProcessingWorker(
            DocumentProcessingQueue processingQueue,
            DocumentProcessingJobRepository jobRepository,
            DocumentExtractionService extractionService
    ) {
        this.processingQueue = processingQueue;
        this.jobRepository = jobRepository;
        this.extractionService = extractionService;
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
}