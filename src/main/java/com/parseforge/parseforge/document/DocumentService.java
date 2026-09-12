package com.parseforge.parseforge.document;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document createDocument(String fileName, String contentType, Long fileSize) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Document document = new Document(id, fileName, contentType, fileSize, DocumentStatus.UPLOADED, now, now);

        return documentRepository.save(document);
    }
}
