package com.parseforge.parseforge.document;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public Document(UUID id, String fileName, String contentType, Long fileSize, String status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
