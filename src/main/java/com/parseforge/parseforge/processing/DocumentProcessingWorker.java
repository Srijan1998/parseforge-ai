package com.parseforge.parseforge.processing;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class DocumentProcessingWorker {

    private final DocumentProcessingQueue processingQueue;
    private final DocumentProcessingJobRepository jobRepository;

    public DocumentProcessingWorker(
            DocumentProcessingQueue processingQueue,
            DocumentProcessingJobRepository jobRepository
    ) {
        this.processingQueue = processingQueue;
        this.jobRepository = jobRepository;
    }

    public void processNextJob() {
        UUID jobId = processingQueue.dequeue();

        if (jobId == null) {
            return;
        }

        DocumentProcessingJob job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Processing job not found: " + jobId
                        )
                );

        job.setStatus(ProcessingJobStatus.PROCESSING);
        job.setUpdatedAt(OffsetDateTime.now());

        jobRepository.save(job);
    }
}