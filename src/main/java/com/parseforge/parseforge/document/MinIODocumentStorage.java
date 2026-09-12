package com.parseforge.parseforge.document;

import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.minio.MinioClient;

import java.io.InputStream;

@Component
public class MinIODocumentStorage implements DocumentStorage{

    private final MinioClient minioClient;
    private final String bucket;

    public MinIODocumentStorage(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket}") String bucket
    ) {
        this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();

        this.bucket = bucket;
    }

    @Override
    public void store(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to store document in MinIO", e);
        }
    }
}
