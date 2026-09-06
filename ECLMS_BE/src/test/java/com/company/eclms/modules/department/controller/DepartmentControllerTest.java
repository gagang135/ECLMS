package com.company.eclms.modules.department.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.department.dto.DepartmentDto;
import com.company.eclms.modules.department.repository.DepartmentRepository;
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

class DepartmentControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"DEPARTMENT_CREATE", "DEPARTMENT_READ", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE"})
    void departmentCrud_Success() throws Exception {
        // 1. Create Department
        DepartmentDto createDto = DepartmentDto.builder()
                .name("Engineering")
                .description("Software engineering department")
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID deptId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Department
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/departments/" + deptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"));

        // 3. Update Department
        DepartmentDto updateDto = DepartmentDto.builder()
                .name("Engineering & QA")
                .description("Engineering and quality assurance department")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/departments/" + deptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering & QA"));

        // 4. Delete Department
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/departments/" + deptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify deleted (Not Found)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/departments/" + deptId))
                .andExpect(status().isNotFound());
    }
}
