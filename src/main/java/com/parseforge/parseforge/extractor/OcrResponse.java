package com.parseforge.parseforge.extractor;

public record OcrResponse(
        String text,
        String method,
        long size
) {
}