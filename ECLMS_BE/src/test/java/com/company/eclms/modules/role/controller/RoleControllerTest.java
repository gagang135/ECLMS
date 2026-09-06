package com.company.eclms.modules.role.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.role.dto.RoleDto;
import com.company.eclms.modules.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE", "ROLE_ASSIGN_PERMISSION"})
    void roleCrud_Success() throws Exception {
        // 1. Create Role
        RoleDto createDto = RoleDto.builder()
                .name("MANAGER")
                .description("Manager Role")
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("MANAGER"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID roleId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Role
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("MANAGER"));

        // 3. Update Role
        RoleDto updateDto = RoleDto.builder()
                .name("SENIOR_MANAGER")
                .description("Senior Manager Role")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("SENIOR_MANAGER"));

        // 4. Assign Permissions (Empty permission IDs just to verify flow)
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/roles/" + roleId + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Delete Role
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Verify deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/roles/" + roleId))
                .andExpect(status().isNotFound());
    }
}
