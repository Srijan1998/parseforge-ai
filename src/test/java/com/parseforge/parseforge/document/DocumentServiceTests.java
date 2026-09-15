package com.parseforge.parseforge.document;

import com.parseforge.parseforge.deduplication.DocumentDeduplicationCache;
import com.parseforge.parseforge.storage.DocumentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTests {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private DocumentStorage documentStorage;

    @Mock
    private DocumentHasher documentHasher;

    @Mock
    private DocumentDeduplicationCache documentDeduplicationCache;

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

        stubForUpload();

        Document savedDocument = documentService.uploadDocument(file).document();

        assertSavedDocument(savedDocument, content);

        verify(documentStorage).store(
                eq(savedDocument.getObjectKey()),
                any(InputStream.class),
                eq((long) content.length),
                eq("application/pdf")
        );

        verify(documentRepository).save(savedDocument);
    }

    @Test
    void shouldNotCreateAndUploadDocumentTwiceWithSameFileName() throws Exception {
        byte[] content = "sample pdf content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                content
        );

        stubForUpload();

        Document savedDocument = documentService.uploadDocument(file).document();

        assertSavedDocument(savedDocument, content);

        when(documentDeduplicationCache.findDocumentWithHash(anyString())).thenReturn(Optional.of(savedDocument.getId()));

        when(documentRepository.findById(savedDocument.getId())).thenReturn(Optional.of(savedDocument));

        savedDocument = documentService.uploadDocument(file).document();

        assertSavedDocument(savedDocument, content);

        verify(documentStorage, times(1)).store(
                anyString(),
                any(InputStream.class),
                eq((long) content.length),
                eq("application/pdf")
        );

        verify(documentRepository, times(1)).save(savedDocument);
    }

    @Test
    void shouldNotCreateAndUploadDocumentWhichIsNotInDeduplicationCacheButExists() throws Exception {
        byte[] content = "sample pdf content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                content
        );

        Document savedDocument = new Document(UUID.randomUUID(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                DocumentStatus.UPLOADED,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "",
                "a".repeat(64));

        when(documentHasher.sha256(any(byte[].class))).thenReturn("a".repeat(64));

        when(documentDeduplicationCache.findDocumentWithHash(anyString())).thenReturn(Optional.empty());

        when(documentRepository.findByContentHash(anyString())).thenReturn(Optional.of(savedDocument));

        savedDocument = documentService.uploadDocument(file).document();

        assertSavedDocument(savedDocument, content);

        verify(documentStorage, never()).store(
                anyString(),
                any(InputStream.class),
                eq((long) content.length),
                eq("application/pdf")
        );

        verify(documentRepository, never()).save(savedDocument);
    }

    @Test
    void shouldNotCreateAndUploadDocumentTwiceWithDifferentFileName() throws Exception {
        byte[] content = "sample pdf content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                content
        );

        stubForUpload();

        Document savedDocument = documentService.uploadDocument(file).document();

        assertSavedDocument(savedDocument, content);

        when(documentDeduplicationCache.findDocumentWithHash(anyString())).thenReturn(Optional.of(savedDocument.getId()));

        when(documentRepository.findById(savedDocument.getId())).thenReturn(Optional.of(savedDocument));

        MockMultipartFile file2 = new MockMultipartFile(
                "file2",
                "invoice.pdf",
                "application/pdf",
                content
        );

        savedDocument = documentService.uploadDocument(file2).document();

        assertSavedDocument(savedDocument, content);

        verify(documentStorage, times(1)).store(
                anyString(),
                any(InputStream.class),
                eq((long) content.length),
                eq("application/pdf")
        );

        verify(documentRepository, times(1)).save(savedDocument);
    }

    private void stubForUpload() {
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(documentHasher.sha256(any(byte[].class))).thenReturn("a".repeat(64));

        when(documentDeduplicationCache.findDocumentWithHash(anyString())).thenReturn(Optional.empty());

        when(documentRepository.findByContentHash(anyString())).thenReturn(Optional.empty());
    }

    private void assertSavedDocument(Document savedDocument, byte[] content) {
        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getFileName()).isEqualTo("invoice.pdf");
        assertThat(savedDocument.getContentType()).isEqualTo("application/pdf");
        assertThat(savedDocument.getFileSize()).isEqualTo(content.length);
        assertThat(savedDocument.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(savedDocument.getCreatedAt()).isNotNull();
        assertThat(savedDocument.getUpdatedAt()).isNotNull();
        assertThat(savedDocument.getContentHash()).isNotNull();
        assertThat(savedDocument.getContentHash()).hasSize(64);
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
