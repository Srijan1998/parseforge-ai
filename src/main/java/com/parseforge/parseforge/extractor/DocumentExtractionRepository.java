package com.parseforge.parseforge.extractor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentExtractionRepository
        extends JpaRepository<DocumentExtraction, UUID> {
}