CREATE TABLE document_extractions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    extracted_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_document_extraction_document
    FOREIGN KEY (document_id)
    REFERENCES documents(id)
);