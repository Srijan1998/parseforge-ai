package com.parseforge.parseforge.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateDocumentRequest(
        @NotBlank String fileName,
        @NotBlank String contentType,
        @Positive Long fileSize
) {

}