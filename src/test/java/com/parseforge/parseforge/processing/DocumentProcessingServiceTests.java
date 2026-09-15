package com.parseforge.parseforge.processing;

import com.parseforge.parseforge.document.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTests {

    @Mock
    private DocumentProcessingJobRepository jobRepository;

    @Mock
    private DocumentProcessingQueue processingQueue;

    @InjectMocks
    private DocumentProcessingService processingService;

    @Test
    void shouldCreateAndEnqueueProcessingJob() {
        Document document = new Document();
        document.setId(UUID.randomUUID());

        when(jobRepository.save(any(DocumentProcessingJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentProcessingJob result =
                processingService.createJob(document);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDocument()).isSameAs(document);
        assertThat(result.getStatus())
                .isEqualTo(ProcessingJobStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(jobRepository).save(any(DocumentProcessingJob.class));
        verify(processingQueue).enqueue(result.getId());
    }
}
