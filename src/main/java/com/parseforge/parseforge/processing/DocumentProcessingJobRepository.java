package com.parseforge.parseforge.processing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentProcessingJobRepository extends JpaRepository<DocumentProcessingJob, UUID> {
}
