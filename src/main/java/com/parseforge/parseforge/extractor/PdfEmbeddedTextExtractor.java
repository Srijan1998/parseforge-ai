package com.parseforge.parseforge.extractor;

import com.parseforge.parseforge.processing.PdfExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfEmbeddedTextExtractor implements DocumentTextExtractor {

    @Override
    public TextExtractionResult extract(InputStream inputStream) {
        try (
                PDDocument document =
                        Loader.loadPDF(inputStream.readAllBytes())
        ) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            return new TextExtractionResult(
                    text,
                    ExtractionMethod.EMBEDDED_TEXT
            );
        } catch (IOException e) {
            throw new PdfExtractionException(
                    "Failed to extract text from PDF",
                    e
            );
        }
    }
}