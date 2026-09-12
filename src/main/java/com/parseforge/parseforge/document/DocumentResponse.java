package com.parseforge.parseforge.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String fileName,
        String contentType,
        long fileSize,
        DocumentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String objectKey
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getObjectKey()
        );
    }
}