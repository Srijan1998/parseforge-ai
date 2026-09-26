package com.parseforge.parseforge.extractor;

public interface DocumentOcrService {
    TextExtractionResult extract(byte[] document);
}
