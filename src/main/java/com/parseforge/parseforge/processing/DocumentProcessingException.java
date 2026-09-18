package com.parseforge.parseforge.processing;

public abstract class DocumentProcessingException extends RuntimeException {

    private final String errorCode;
    private final boolean retryable;

    protected DocumentProcessingException(
            String errorCode,
            String message,
            boolean retryable,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}