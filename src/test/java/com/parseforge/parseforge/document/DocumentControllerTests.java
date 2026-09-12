package com.parseforge.parseforge.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void shouldCreateDocument() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Document document = new Document(id, "invoice.pdf", "application/pdf", 2048L, DocumentStatus.UPLOADED, now, now, "");

        when(documentService.createDocument(eq("invoice.pdf"), eq("application/pdf"), eq(2048L))).thenReturn(document);

        mockMvc.perform(post("/api/documents")
                .contentType("application/json")
                .content("""
                        {
                            "fileName": "invoice.pdf",
                            "contentType": "application/pdf",
                            "fileSize": 2048
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fileName").value("invoice.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(2048))
                .andExpect(jsonPath("$.status").value("UPLOADED"));

    }
}
