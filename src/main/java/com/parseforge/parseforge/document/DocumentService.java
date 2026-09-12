package com.parseforge.parseforge.document;

import com.parseforge.parseforge.storage.DocumentStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;

    public DocumentService(DocumentRepository documentRepository, DocumentStorage documentStorage) {
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
    }

    public Document createDocument(String fileName, String contentType, Long fileSize) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Document document = new Document(id, fileName, contentType, fileSize, DocumentStatus.UPLOADED, now, now, "");

        return documentRepository.save(document);
    }

    public Document uploadDocument(MultipartFile file) {
        UUID documentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        String objectKey = "documents/" + documentId + "/" + file.getOriginalFilename();

        try {
            documentStorage.store(
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read uploaded document", e);
        }
        Document document = new Document(documentId, file.getOriginalFilename(), file.getContentType(), file.getSize(), DocumentStatus.UPLOADED, now, now, objectKey);

        return documentRepository.save(document);
    }
}
