ALTER TABLE document_extractions
    ADD COLUMN extraction_method VARCHAR(50) NOT NULL DEFAULT 'EMBEDDED_TEXT';