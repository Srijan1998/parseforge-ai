package com.parseforge.parseforge.processing;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RedisDocumentProcessingQueue
        implements DocumentProcessingQueue {

    private static final String QUEUE_KEY =
            "parseforge:processing:queue";

    private final StringRedisTemplate redisTemplate;

    public RedisDocumentProcessingQueue(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void enqueue(UUID jobId) {
        redisTemplate.opsForList()
                .rightPush(QUEUE_KEY, jobId.toString());
    }
}
