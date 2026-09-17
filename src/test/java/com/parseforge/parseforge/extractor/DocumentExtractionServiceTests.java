package com.parseforge.parseforge.extractor;

import com.parseforge.parseforge.document.Document;
import com.parseforge.parseforge.storage.DocumentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentExtractionServiceTests {

    @Mock
    private DocumentStorage documentStorage;

    @Mock
    private DocumentTextExtractor textExtractor;

    @Mock
    private DocumentExtractionRepository extractionRepository;

    @InjectMocks
    private DocumentExtractionService extractionService;

    @Test
    void shouldExtractAndPersistDocumentText() {
        Document document = new Document();
        document.setId(UUID.randomUUID());
        document.setObjectKey("documents/test.pdf");

        InputStream stream =
                new ByteArrayInputStream("pdf".getBytes());

        when(documentStorage.retrieve(document.getObjectKey()))
                .thenReturn(stream);

        when(textExtractor.extract(stream))
                .thenReturn("Invoice number 12345");

        when(extractionRepository.save(any(DocumentExtraction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentExtraction result =
                extractionService.extract(document);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDocument()).isSameAs(document);
        assertThat(result.getExtractedText())
                .isEqualTo("Invoice number 12345");
        assertThat(result.getCreatedAt()).isNotNull();

        verify(documentStorage)
                .retrieve(document.getObjectKey());

        verify(textExtractor)
                .extract(stream);

        verify(extractionRepository)
                .save(any(DocumentExtraction.class));
    }
}
