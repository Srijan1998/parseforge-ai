package com.parseforge.parseforge.processing;

import java.util.UUID;

public interface DocumentProcessingQueue {

    void enqueue(UUID jobId);

    UUID dequeue();
}
