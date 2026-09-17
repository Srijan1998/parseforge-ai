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
            job.setStatus(ProcessingJobStatus.PROCESSING);
            job.setUpdatedAt(OffsetDateTime.now());
            jobRepository.save(job);

            extractionService.extract(job.getDocument());

            job.setStatus(ProcessingJobStatus.COMPLETED);
            job.setUpdatedAt(OffsetDateTime.now());
            jobRepository.save(job);

        } catch (Exception e) {
            log.error(
                    "Document processing failed for jobId={}, documentId={}",
                    job.getId(),
                    job.getDocument().getId(),
                    e
            );
            job.setStatus(ProcessingJobStatus.FAILED);
            job.setUpdatedAt(OffsetDateTime.now());
            jobRepository.save(job);
        }
    }
}