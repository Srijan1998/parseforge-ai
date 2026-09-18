ALTER TABLE document_processing_jobs
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE document_processing_jobs
    ADD COLUMN started_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE document_processing_jobs
    ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE document_processing_jobs
    ADD COLUMN error_code VARCHAR(100);

ALTER TABLE document_processing_jobs
    ADD COLUMN error_message TEXT;