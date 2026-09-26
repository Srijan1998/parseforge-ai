package com.parseforge.parseforge.extractor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class FastApiDocumentOcrService implements DocumentOcrService {

    private final RestClient restClient;

    public FastApiDocumentOcrService(RestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public TextExtractionResult extract(byte[] document) {
        ByteArrayResource resource = new ByteArrayResource(document) {
            @Override
            public String getFilename() {
                return "document.pdf";
            }
        };
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", resource)
                .filename("document.pdf")
                .contentType(MediaType.APPLICATION_PDF);

        OcrResponse response = restClient.post()
                                        .uri("/ocr")
                                        .contentType(MediaType.MULTIPART_FORM_DATA)
                                        .body(builder.build())
                                        .retrieve()
                                        .body(OcrResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "AI service returned an empty response"
            );
        }

        return new TextExtractionResult(
                response.text(),
                ExtractionMethod.OCR
        );
    }
}