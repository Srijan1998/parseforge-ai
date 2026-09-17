package com.parseforge.parseforge.extractor;

import com.parseforge.parseforge.document.Document;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_extractions")
@Getter
@Setter
@NoArgsConstructor
public class DocumentExtraction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "extracted_text", nullable = false, columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
