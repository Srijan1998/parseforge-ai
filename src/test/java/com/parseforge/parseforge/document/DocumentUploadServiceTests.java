package com.parseforge.parseforge.document;

import com.parseforge.parseforge.processing.DocumentProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTests {

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentProcessingService processingService;

    @InjectMocks
    private DocumentUploadService documentUploadService;

    @Test
    void shouldCreateProcessingJobForNewDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        Document document = new Document();
        document.setId(UUID.randomUUID());

        when(documentService.uploadDocument(file))
                .thenReturn(new DocumentUploadResult(document, true));

        Document result = documentUploadService.upload(file);

        assertThat(result).isSameAs(document);

        verify(processingService).createJob(document);
    }

    @Test
    void shouldNotCreateProcessingJobForDuplicateDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        Document document = new Document();
        document.setId(UUID.randomUUID());

        when(documentService.uploadDocument(file))
                .thenReturn(new DocumentUploadResult(document, false));

        Document result = documentUploadService.upload(file);

        assertThat(result).isSameAs(document);

        verify(processingService, never())
                .createJob(any(Document.class));
    }
}