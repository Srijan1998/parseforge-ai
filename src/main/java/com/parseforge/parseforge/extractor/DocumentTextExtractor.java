package com.parseforge.parseforge.extractor;

import java.io.InputStream;

public interface DocumentTextExtractor {

    TextExtractionResult extract(InputStream inputStream);
}