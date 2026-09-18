package com.parseforge.parseforge.processing;

import com.parseforge.parseforge.document.Document;
import com.parseforge.parseforge.extractor.DocumentExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingWorkerTests {

    @Mock
    private DocumentProcessingQueue processingQueue;

    @Mock
    private DocumentProcessingJobRepository jobRepository;

    private DocumentProcessingWorker worker;

    @Mock
    private DocumentExtractionService extractionService;

    @BeforeEach
    void setUp() {
        worker = new DocumentProcessingWorker(
                processingQueue,
                jobRepository,
                extractionService,
                3
        );
    }

    @Test
    void shouldMoveDequeuedJobToProcessing() {
        UUID jobId = UUID.randomUUID();

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(jobId);
        job.setAttemptCount(0);
        job.setStatus(ProcessingJobStatus.PENDING);

        when(processingQueue.dequeue())
                .thenReturn(jobId);

        when(jobRepository.findByIdWithDocument(jobId))
                .thenReturn(Optional.of(job));

        worker.processNextJob();

        assertThat(job.getUpdatedAt())
                .isNotNull();

        verify(extractionService).extract(job.getDocument());

        assertThat(job.getStatus())
                .isEqualTo(ProcessingJobStatus.COMPLETED);

        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getStartedAt()).isNotNull();
        assertThat(job.getCompletedAt()).isNotNull();
        assertThat(job.getErrorCode()).isNull();
        assertThat(job.getErrorMessage()).isNull();

        verify(jobRepository, times(2)).save(job);
    }

    @Test
    void shouldDoNothingWhenQueueIsEmpty() {
        when(processingQueue.dequeue())
                .thenReturn(null);

        worker.processNextJob();

        verifyNoInteractions(jobRepository);
    }

    @Test
    void shouldMarkJobAsFailedWhenExtractionFails() {
        UUID jobId = UUID.randomUUID();

        Document document = new Document();
        document.setId(UUID.randomUUID());

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(jobId);
        job.setAttemptCount(0);
        job.setDocument(document);
        job.setStatus(ProcessingJobStatus.PENDING);

        when(processingQueue.dequeue())
                .thenReturn(jobId);

        when(jobRepository.findByIdWithDocument(jobId))
                .thenReturn(Optional.of(job));

        doThrow(new IllegalStateException("Extraction failed"))
                .when(extractionService)
                .extract(document);

        worker.processNextJob();

        assertThat(job.getStatus())
                .isEqualTo(ProcessingJobStatus.FAILED);

        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getStartedAt()).isNotNull();
        assertThat(job.getCompletedAt()).isNull();

        assertThat(job.getErrorCode())
                .isEqualTo("PROCESSING_FAILED");

        assertThat(job.getErrorMessage())
                .isEqualTo("Extraction failed");

        verify(extractionService).extract(document);
        verify(jobRepository, times(2)).save(job);
    }

    @Test
    void shouldPersistSpecificProcessingFailure() {
        UUID jobId = UUID.randomUUID();

        Document document = new Document();
        document.setId(UUID.randomUUID());

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(jobId);
        job.setDocument(document);
        job.setStatus(ProcessingJobStatus.PENDING);
        job.setAttemptCount(0);

        when(processingQueue.dequeue())
                .thenReturn(jobId);

        when(jobRepository.findByIdWithDocument(jobId))
                .thenReturn(Optional.of(job));

        doThrow(new PdfExtractionException(
                "Failed to extract text from PDF",
                new IOException("Invalid PDF")
        )).when(extractionService).extract(document);

        worker.processNextJob();

        assertThat(job.getStatus())
                .isEqualTo(ProcessingJobStatus.FAILED);

        assertThat(job.getErrorCode())
                .isEqualTo("PDF_EXTRACTION_FAILED");

        assertThat(job.getErrorMessage())
                .isEqualTo("Failed to extract text from PDF");

        assertThat(job.getAttemptCount())
                .isEqualTo(1);
    }

    @Test
    void shouldRequeueRetryableFailureWhenAttemptsRemain() {
        UUID jobId = UUID.randomUUID();

        Document document = new Document();
        document.setId(UUID.randomUUID());

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(jobId);
        job.setDocument(document);
        job.setStatus(ProcessingJobStatus.PENDING);
        job.setAttemptCount(0);

        when(processingQueue.dequeue()).thenReturn(jobId);

        when(jobRepository.findByIdWithDocument(jobId))
                .thenReturn(Optional.of(job));

        doThrow(new DocumentStorageException(
                "MinIO temporarily unavailable",
                new RuntimeException("Connection refused")
        )).when(extractionService).extract(document);

        worker.processNextJob();

        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getStatus())
                .isEqualTo(ProcessingJobStatus.PENDING);

        assertThat(job.getErrorCode())
                .isEqualTo("STORAGE_RETRIEVAL_FAILED");

        verify(processingQueue).enqueue(jobId);
    }

    @Test
    void shouldFailRetryableJobAfterMaximumAttempts() {
        UUID jobId = UUID.randomUUID();

        Document document = new Document();
        document.setId(UUID.randomUUID());

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(jobId);
        job.setDocument(document);
        job.setStatus(ProcessingJobStatus.PENDING);

        // This execution becomes attempt #3.
        job.setAttemptCount(2);

        when(processingQueue.dequeue()).thenReturn(jobId);

        when(jobRepository.findByIdWithDocument(jobId))
                .thenReturn(Optional.of(job));

        doThrow(new DocumentStorageException(
                "MinIO unavailable",
                new RuntimeException("Connection refused")
        )).when(extractionService).extract(document);

        worker.processNextJob();

        assertThat(job.getAttemptCount()).isEqualTo(3);
        assertThat(job.getStatus())
                .isEqualTo(ProcessingJobStatus.FAILED);

        verify(processingQueue, never()).enqueue(jobId);
    }
}
