package com.parseforge.parseforge.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DocumentRepositoryTests {

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void shouldSaveAndRetrieveDocument() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Document document = new Document(id, "invoice.pdf", "application/pdf", 1024L, DocumentStatus.UPLOADED, now, now, "");

        documentRepository.save(document);

        var savedDocument = documentRepository.findById(id);
        assertThat(savedDocument).isPresent();
        assertThat(savedDocument.get().getFileName()).isEqualTo("invoice.pdf");
        assertThat(savedDocument.get().getContentType()).isEqualTo("application/pdf");
        assertThat(savedDocument.get().getFileSize()).isEqualTo(1024L);
        assertThat(savedDocument.get().getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    }
}
