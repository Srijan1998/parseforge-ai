package com.parseforge.parseforge.document;

public record DocumentUploadResult(
        Document document,
        boolean created
) {
}