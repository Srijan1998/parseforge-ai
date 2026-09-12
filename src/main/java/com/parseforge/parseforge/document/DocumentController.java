package com.parseforge.parseforge.document;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        Document document = documentService.createDocument(request.fileName(), request.contentType(), request.fileSize());

        return DocumentResponse.from(document);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse uploadDocument(
            @RequestParam("file")MultipartFile file
    ) {
        return DocumentResponse.from(
                documentService.uploadDocument(file)
        );
    }
}
