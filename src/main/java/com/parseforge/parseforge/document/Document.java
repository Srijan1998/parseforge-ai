package com.parseforge.parseforge.document;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
public class Document {
    @Id
    private UUID id;

    private String fileName;

    private String contentType;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private String objectKey;

    public Document(UUID id, String fileName, String contentType, Long fileSize, DocumentStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt, String objectKey) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.objectKey = objectKey;
    }
}
