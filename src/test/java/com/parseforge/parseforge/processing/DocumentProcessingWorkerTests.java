package com.parseforge.parseforge.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private DocumentProcessingWorker worker;

    @Test
    void shouldMoveDequeuedJobToProcessing() {
        UUID jobId = UUID.randomUUID();

        DocumentProcessingJob job = new DocumentProcessingJob();
        job.setId(jobId);
        job.setStatus(ProcessingJobStatus.PENDING);

        when(processingQueue.dequeue())
                .thenReturn(jobId);

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        worker.processNextJob();

        assertThat(job.getStatus())
                .isEqualTo(ProcessingJobStatus.PROCESSING);

        assertThat(job.getUpdatedAt())
                .isNotNull();

        verify(jobRepository).save(job);
    }

    @Test
    void shouldDoNothingWhenQueueIsEmpty() {
        when(processingQueue.dequeue())
                .thenReturn(null);

        worker.processNextJob();

        verifyNoInteractions(jobRepository);
    }
}
