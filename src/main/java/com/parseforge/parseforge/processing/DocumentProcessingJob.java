package com.parseforge.parseforge.processing;

import com.parseforge.parseforge.document.Document;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_processing_jobs")
@Getter
@Setter
@NoArgsConstructor
public class DocumentProcessingJob {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    private ProcessingJobStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}