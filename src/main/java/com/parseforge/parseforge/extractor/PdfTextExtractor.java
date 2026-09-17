package com.parseforge.parseforge.extractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfTextExtractor implements DocumentTextExtractor {

    @Override
    public String extract(InputStream inputStream) {
        try (
                PDDocument document =
                        Loader.loadPDF(inputStream.readAllBytes())
        ) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to extract text from PDF",
                    e
            );
        }
    }
}