package com.parseforge.parseforge.processing;

import com.parseforge.parseforge.document.Document;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DocumentProcessingService {

    private final DocumentProcessingJobRepository jobRepository;
    private final DocumentProcessingQueue processingQueue;

    public DocumentProcessingService(
            DocumentProcessingJobRepository jobRepository,
            DocumentProcessingQueue processingQueue
    ) {
        this.jobRepository = jobRepository;
        this.processingQueue = processingQueue;
    }

    public DocumentProcessingJob createJob(Document document) {
        OffsetDateTime now = OffsetDateTime.now();

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(UUID.randomUUID());
        job.setDocument(document);
        job.setStatus(ProcessingJobStatus.PENDING);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);

        DocumentProcessingJob savedJob = jobRepository.save(job);

        processingQueue.enqueue(savedJob.getId());

        return savedJob;
    }
}