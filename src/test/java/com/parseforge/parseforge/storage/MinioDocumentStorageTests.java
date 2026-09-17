package com.parseforge.parseforge.storage;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MinioDocumentStorageTests {

    @Mock
    private MinioClient minioClient;

    private String bucket = "bucket";

    private MinIODocumentStorage documentStorage;

    @BeforeEach
    void setUp() {
        documentStorage = new MinIODocumentStorage(minioClient, bucket);
    }

    @Test
    void retrievalShouldCallMinioGet() {
        when(documentStorage.retrieve("test")).thenReturn(mock(GetObjectResponse.class));
        try (InputStream ignored = documentStorage.retrieve("test")) {
            verify(minioClient).getObject(any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
