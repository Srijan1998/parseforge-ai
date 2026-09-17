package com.parseforge.parseforge.extractor;

import com.parseforge.parseforge.document.Document;
import com.parseforge.parseforge.storage.DocumentStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DocumentExtractionService {

    private final DocumentStorage documentStorage;
    private final DocumentTextExtractor textExtractor;
    private final DocumentExtractionRepository extractionRepository;

    public DocumentExtractionService(
            DocumentStorage documentStorage,
            DocumentTextExtractor textExtractor,
            DocumentExtractionRepository extractionRepository
    ) {
        this.documentStorage = documentStorage;
        this.textExtractor = textExtractor;
        this.extractionRepository = extractionRepository;
    }

    public DocumentExtraction extract(Document document) {

        String extractedText;

        try (InputStream inputStream =
                     documentStorage.retrieve(document.getObjectKey())) {

            extractedText = textExtractor.extract(inputStream);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to close document stream",
                    e
            );
        }

        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setId(UUID.randomUUID());
        extraction.setDocument(document);
        extraction.setExtractedText(extractedText);
        extraction.setCreatedAt(OffsetDateTime.now());

        return extractionRepository.save(extraction);
    }
}
