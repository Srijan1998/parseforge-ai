package com.parseforge.parseforge.processing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DocumentProcessingJobRepository extends JpaRepository<DocumentProcessingJob, UUID> {

    @Query("""
            SELECT j from DocumentProcessingJob j
            JOIN FETCH j.document
            where j.id = :id
            """)
    Optional<DocumentProcessingJob> findByIdWithDocument(
            @Param("id") UUID id
    );
}
