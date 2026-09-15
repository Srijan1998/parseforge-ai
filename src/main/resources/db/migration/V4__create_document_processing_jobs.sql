CREATE TABLE document_processing_jobs (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_processing_job_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
);