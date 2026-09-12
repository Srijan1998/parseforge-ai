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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void shouldFailEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> documentService.uploadDocument(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("File cannot be empty");
    }

    @Test
    void shouldFailOnEmptyFileName() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> documentService.uploadDocument(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("File name cannot be empty");
    }

    @Test
    void shouldFailOnInvalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.json",
                "application/json",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> documentService.uploadDocument(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("File can only be pdf");
    }

    @Test
    void shouldFailOnFileSizeGreaterThanMax() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.json",
                "application/json",
                new byte[10 * 10 * 24 + 1]
        );

        assertThatThrownBy(() -> documentService.uploadDocument(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("File can only be pdf");
    }
}
