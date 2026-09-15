package com.parseforge.parseforge.document;

import com.parseforge.parseforge.processing.DocumentProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentUploadService {

    private final DocumentService documentService;
    private final DocumentProcessingService processingService;

    public DocumentUploadService(
            DocumentService documentService,
            DocumentProcessingService processingService
    ) {
        this.documentService = documentService;
        this.processingService = processingService;
    }

    public Document upload(MultipartFile file) {

        DocumentUploadResult result =
                documentService.uploadDocument(file);

        if (result.created()) {
            processingService.createJob(result.document());
        }

        return result.document();
    }
}
