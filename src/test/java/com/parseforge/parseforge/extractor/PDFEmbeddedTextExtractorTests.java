package com.parseforge.parseforge.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PDFEmbeddedTextExtractorTests {

    private final PdfEmbeddedTextExtractor extractor =
            new PdfEmbeddedTextExtractor();

    @Test
    void shouldExtractTextFromPdf() throws Exception {
        byte[] pdf;

        try (
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();

                PDDocument document =
                        new PDDocument()
        ) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        12
                );
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("ParseForge invoice 12345");
                contentStream.endText();
            }

            document.save(output);
            pdf = output.toByteArray();
        }

        TextExtractionResult result =
                extractor.extract(
                        new ByteArrayInputStream(pdf)
                );

        assertThat(result.text())
                .contains("ParseForge invoice 12345");

        assertThat(result.method())
                .isEqualTo(ExtractionMethod.EMBEDDED_TEXT);
    }
}