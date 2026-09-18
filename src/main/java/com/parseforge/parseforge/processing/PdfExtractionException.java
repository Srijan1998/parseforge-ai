package com.parseforge.parseforge.processing;

public class PdfExtractionException
        extends DocumentProcessingException {

    public PdfExtractionException(
            String message,
            Throwable cause
    ) {
        super(
                "PDF_EXTRACTION_FAILED",
                message,
                false,
                cause
        );
    }
}
