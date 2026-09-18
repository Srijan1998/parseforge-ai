package com.parseforge.parseforge.processing;

public class DocumentStorageException
        extends DocumentProcessingException {

    public DocumentStorageException(
            String message,
            Throwable cause
    ) {
        super(
                "STORAGE_RETRIEVAL_FAILED",
                message,
                true,
                cause
        );
    }
}
