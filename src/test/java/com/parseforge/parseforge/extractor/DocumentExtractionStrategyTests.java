package com.parseforge.parseforge.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentExtractionStrategyTests {
    @Mock
    private DocumentTextExtractor documentTextExtractor;

    @Mock
    private DocumentOcrService ocrService;

    @InjectMocks
    private DocumentExtractionStrategy extractionStrategy;

    @Test
    void shouldUseEmbeddedTextWhenEnoughTextExists() {
        byte[] document = "pdf".getBytes();

        String text =
                "This PDF contains plenty of embedded text for ParseForge processing.";

        when(documentTextExtractor.extract(any(ByteArrayInputStream.class)))
                .thenReturn(
                        new TextExtractionResult(
                                text,
                                ExtractionMethod.EMBEDDED_TEXT
                        )
                );

        TextExtractionResult result =
                extractionStrategy.extract(document);

        assertThat(result.method())
                .isEqualTo(ExtractionMethod.EMBEDDED_TEXT);

        verifyNoInteractions(ocrService);
    }

    @Test
    void shouldFallbackToOcrWhenEmbeddedTextIsInsufficient() {
        byte[] document = "pdf".getBytes();

        when(documentTextExtractor.extract(any(ByteArrayInputStream.class)))
                .thenReturn(
                        new TextExtractionResult(
                                "",
                                ExtractionMethod.EMBEDDED_TEXT
                        )
                );

        when(ocrService.extract(document))
                .thenReturn(
                        new TextExtractionResult(
                                "Text extracted using OCR",
                                ExtractionMethod.OCR
                        )
                );

        TextExtractionResult result =
                extractionStrategy.extract(document);

        assertThat(result.text())
                .isEqualTo("Text extracted using OCR");

        assertThat(result.method())
                .isEqualTo(ExtractionMethod.OCR);

        verify(ocrService).extract(document);
    }
}
