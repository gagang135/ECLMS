package com.company.eclms.modules.document.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"DOCUMENT_UPLOAD", "DOCUMENT_READ", "DOCUMENT_DELETE"})
    void documentCrud_Success() throws Exception {
        // 1. Upload Document
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy pdf content".getBytes()
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/documents/upload")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("contract.pdf"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID documentId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Document Info
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("contract.pdf"));

        // 3. Download Document
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/documents/" + documentId + "/download"))
                .andExpect(status().isOk());

        // 4. Preview Document
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/documents/" + documentId + "/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Delete Document
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Verify deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/documents/" + documentId))
                .andExpect(status().isNotFound());
    }
}
