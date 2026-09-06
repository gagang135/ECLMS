package com.company.eclms.modules.contracttemplate.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.contracttemplate.dto.ContractTemplateDto;
import com.company.eclms.modules.contracttemplate.repository.ContractTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContractTemplateControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private ContractTemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        templateRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"TEMPLATE_CREATE", "TEMPLATE_READ", "TEMPLATE_UPDATE", "TEMPLATE_DELETE", "TEMPLATE_PUBLISH"})
    void templateCrud_Success() throws Exception {
        // 1. Create Template
        ContractTemplateDto createDto = ContractTemplateDto.builder()
                .name("NDA Template")
                .content("This is an NDA between {{party1}} and {{party2}}.")
                .variables("party1,party2")
                .status("DRAFT")
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NDA Template"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID templateId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Template
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/contract-templates/" + templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("This is an NDA between {{party1}} and {{party2}}."));

        // 3. Update Template
        ContractTemplateDto updateDto = ContractTemplateDto.builder()
                .name("Updated NDA Template")
                .content("This updated NDA between {{party1}} and {{party2}}.")
                .variables("party1,party2")
                .status("DRAFT")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/contract-templates/" + templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated NDA Template"));

        // 4. Publish Template
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contract-templates/" + templateId + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // 5. Archive Template
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contract-templates/" + templateId + "/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        // 6. Delete Template
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/contract-templates/" + templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/contract-templates/" + templateId))
                .andExpect(status().isNotFound());
    }
}
