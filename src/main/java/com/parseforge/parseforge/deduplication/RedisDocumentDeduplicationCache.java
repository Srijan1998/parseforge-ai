package com.parseforge.parseforge.deduplication;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RedisDocumentDeduplicationCache implements DocumentDeduplicationCache {

    private static final String KEY_PREFIX = "parseforge:dedup:";
    private final StringRedisTemplate redisTemplate;

    public RedisDocumentDeduplicationCache(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<UUID> findDocumentWithHash(String contentHash) {
        String documentId = redisTemplate.opsForValue().get(KEY_PREFIX + contentHash);

        if (documentId == null) {
            return Optional.empty();
        }

        return Optional.of(UUID.fromString(documentId));
    }

    @Override
    public void store(String contentHash, UUID documentId) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX +contentHash,
                documentId.toString()
        );
    }
}
