package com.parseforge.parseforge.document;

import com.parseforge.parseforge.storage.DocumentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTests {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private DocumentStorage documentStorage;

    @Test
    void shouldCreateAndSaveDocument() {
        Document document = documentService.createDocument("invoice.pdf", "application/pdf", 1024L);
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        var savedDocument = captor.getValue();
        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getFileName()).isEqualTo("invoice.pdf");
        assertThat(savedDocument.getContentType()).isEqualTo("application/pdf");
        assertThat(savedDocument.getFileSize()).isEqualTo(1024L);
        assertThat(savedDocument.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(savedDocument.getCreatedAt()).isNotNull();
        assertThat(savedDocument.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreateAndUploadDocument() throws Exception {
        byte[] content = "sample pdf content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                content
        );

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Document savedDocument = documentService.uploadDocument(file);

        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getFileName()).isEqualTo("invoice.pdf");
        assertThat(savedDocument.getContentType()).isEqualTo("application/pdf");
        assertThat(savedDocument.getFileSize()).isEqualTo(content.length);
        assertThat(savedDocument.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(savedDocument.getCreatedAt()).isNotNull();
        assertThat(savedDocument.getUpdatedAt()).isNotNull();

        verify(documentStorage).store(
                eq(savedDocument.getObjectKey()),
                any(InputStream.class),
                eq((long) content.length),
                eq("application/pdf")
        );

        verify(documentRepository).save(savedDocument);
    }
}
