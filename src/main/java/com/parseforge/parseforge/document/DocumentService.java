package com.parseforge.parseforge.document;

import com.parseforge.parseforge.deduplication.DocumentDeduplicationCache;
import com.parseforge.parseforge.storage.DocumentStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final DocumentHasher documentHasher;
    private final DocumentDeduplicationCache documentDeduplicationCache;

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentStorage documentStorage,
                           DocumentHasher documentHasher,
                           DocumentDeduplicationCache documentDeduplicationCache) {
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
        this.documentHasher = documentHasher;
        this.documentDeduplicationCache = documentDeduplicationCache;
    }

    public Document createDocument(String fileName, String contentType, Long fileSize) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Document document = new Document(id, fileName, contentType, fileSize, DocumentStatus.UPLOADED, now, now, "", "");

        return documentRepository.save(document);
    }

    public DocumentUploadResult uploadDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidDocumentException("File cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidDocumentException("File cannot exceed 10mb");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new InvalidDocumentException("File can only be pdf");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new InvalidDocumentException("File name cannot be empty");
        }

        UUID documentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        String objectKey = "documents/" + documentId + "/" + originalFileName;

        byte[] content;

        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded document", e);
        }

        String contentHash = documentHasher.sha256(content);

        Optional<UUID> cachedDocumentId = documentDeduplicationCache.findDocumentWithHash(contentHash);

        if (cachedDocumentId.isPresent()) {
            Optional<Document> cachedDocument = documentRepository.findById(cachedDocumentId.get());

            if (cachedDocument.isPresent()) {
                return new DocumentUploadResult(
                        cachedDocument.get(),
                        false
                );
            }
        }

        Optional<Document> existingDocument = documentRepository.findByContentHash(contentHash);

        if (existingDocument.isPresent()) {
            Document document = existingDocument.get();
            documentDeduplicationCache.store(contentHash, document.getId());

            return new DocumentUploadResult(document, false);
        }

        try {
            documentStorage.store(
                    objectKey,
                    new ByteArrayInputStream(content),
                    file.getSize(),
                    contentType
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read uploaded document", e);
        }
        Document document = new Document(documentId, originalFileName, contentType, file.getSize(), DocumentStatus.UPLOADED, now, now, objectKey, contentHash);

        return new DocumentUploadResult(
                documentRepository.save(document),
                true
        );
    }
}
