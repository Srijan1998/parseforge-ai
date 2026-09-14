package com.parseforge.parseforge.deduplication;

import java.util.Optional;
import java.util.UUID;

public interface DocumentDeduplicationCache {
    Optional<UUID> findDocumentWithHash(String contentHash);

    void store(String contentHash, UUID documentId);
}