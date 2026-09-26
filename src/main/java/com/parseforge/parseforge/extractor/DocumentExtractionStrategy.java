package com.parseforge.parseforge.extractor;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class DocumentExtractionStrategy {

    private static final int MIN_TEXT_LENGTH = 50;

    private final DocumentTextExtractor documentTextExtractor;
    private final DocumentOcrService ocrService;

    public DocumentExtractionStrategy(
            DocumentTextExtractor documentTextExtractor,
            DocumentOcrService ocrService
    ) {
        this.documentTextExtractor = documentTextExtractor;
        this.ocrService = ocrService;
    }

    public TextExtractionResult extract(byte[] document) {

        TextExtractionResult embeddedResult =
                documentTextExtractor.extract(
                        new ByteArrayInputStream(document)
                );

        if (hasEnoughText(embeddedResult.text())) {
            return embeddedResult;
        }

        return ocrService.extract(document);
    }

    private boolean hasEnoughText(String text) {
        if (text == null) {
            return false;
        }

        String normalized = text.strip();

        if (normalized.length() < MIN_TEXT_LENGTH) {
            return false;
        }

        long alphanumericCharacters = normalized.chars()
                .filter(Character::isLetterOrDigit)
                .count();

        double alphanumericRatio =
                (double) alphanumericCharacters / normalized.length();

        return alphanumericRatio >= 0.5;
    }
}
