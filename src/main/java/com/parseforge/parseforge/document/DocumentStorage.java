package com.parseforge.parseforge.document;

import java.io.InputStream;

public interface DocumentStorage {

    void store(
            String objectKey,
            InputStream inputStream,
            long size,
            String contentType
    );
}
