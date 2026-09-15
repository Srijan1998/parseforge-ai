package com.parseforge.parseforge.processing;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DocumentProcessingScheduler {

    private final DocumentProcessingWorker processingWorker;

    public DocumentProcessingScheduler(
            DocumentProcessingWorker processingWorker
    ) {
        this.processingWorker = processingWorker;
    }

    @Scheduled(fixedDelayString = "${processing.worker.poll-delay-ms:1000}")
    public void processJobs() {
        processingWorker.processNextJob();
    }
}
