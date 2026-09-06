package com.company.eclms.modules.permission.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.permission.dto.PermissionDto;
import com.company.eclms.modules.permission.repository.PermissionRepository;
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

class PermissionControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        permissionRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_CREATE", "PERMISSION_READ", "PERMISSION_UPDATE", "PERMISSION_DELETE"})
    void permissionCrud_Success() throws Exception {
        // 1. Create Permission
        PermissionDto createDto = PermissionDto.builder()
                .name("CONTRACT_APPROVE_SPECIAL")
                .permissionGroup("CONTRACT")
                .description("Permission to approve contracts with high value")
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("CONTRACT_APPROVE_SPECIAL"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID permissionId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Permission
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/permissions/" + permissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("CONTRACT_APPROVE_SPECIAL"));

        // 3. Update Permission
        PermissionDto updateDto = PermissionDto.builder()
                .name("CONTRACT_APPROVE_SUPER")
                .permissionGroup("CONTRACT")
                .description("Super permission to approve contracts")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/permissions/" + permissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("CONTRACT_APPROVE_SUPER"));

        // 4. Delete Permission
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/permissions/" + permissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/permissions/" + permissionId))
                .andExpect(status().isNotFound());
    }
}
