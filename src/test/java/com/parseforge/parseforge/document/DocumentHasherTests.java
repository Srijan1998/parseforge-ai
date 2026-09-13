package com.parseforge.parseforge.document;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentHasherTests {

    private final DocumentHasher documentHasher = new DocumentHasher();

    @Test
    void shouldGenerateSHA256Hash() {
        String hash = documentHasher.sha256("hello".getBytes());

        assertThat(hash)
                .isEqualTo(
                        "2cf24dba5fb0a30e26e83b2ac5b9e29e" +
                                "1b161e5c1fa7425e73043362938b9824"
                );
    }
}
