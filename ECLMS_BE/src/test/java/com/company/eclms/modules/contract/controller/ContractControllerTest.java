package com.company.eclms.modules.contract.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.contract.dto.ContractDto;
import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.department.entity.Department;
import com.company.eclms.modules.department.repository.DepartmentRepository;
import com.company.eclms.modules.vendor.entity.Vendor;
import com.company.eclms.modules.vendor.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContractControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private VendorRepository vendorRepository;

    private Department department;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
        contractRepository.deleteAll();
        departmentRepository.deleteAll();
        vendorRepository.deleteAll();

        department = new Department();
        department.setName("Legal");
        department = departmentRepository.save(department);

        vendor = new Vendor();
        vendor.setName("Test Vendor");
        vendor.setEmail("vendor@test.com");
        vendor = vendorRepository.save(vendor);
    }

    @Test
    @WithMockUser(authorities = {"CONTRACT_CREATE", "CONTRACT_READ", "CONTRACT_UPDATE", "CONTRACT_DELETE", "CONTRACT_RENEW", "CONTRACT_TERMINATE"})
    void contractCrud_Success() throws Exception {
        // 1. Create Contract
        ContractDto createDto = ContractDto.builder()
                .name("Service Agreement")
                .departmentId(department.getId())
                .vendorId(vendor.getId())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status("DRAFT")
                .content("NDA contract content details")
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Service Agreement"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID contractId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Contract
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/contracts/" + contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Service Agreement"));

        // 3. Update Contract
        ContractDto updateDto = ContractDto.builder()
                .name("Updated Service Agreement")
                .departmentId(department.getId())
                .vendorId(vendor.getId())
                .status("DRAFT")
                .content("Updated NDA contract content details")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/contracts/" + contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Service Agreement"));

        // Update status to ACTIVE in DB to allow renewal
        com.company.eclms.modules.contract.entity.Contract dbContract = contractRepository.findById(contractId).get();
        dbContract.setStatus("ACTIVE");
        contractRepository.save(dbContract);

        // 4. Renew Contract
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contracts/" + contractId + "/renew")
                        .param("newEndDate", LocalDate.now().plusYears(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Terminate Contract
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contracts/" + contractId + "/terminate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("TERMINATED"));

        // 6. Delete Contract
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/contracts/" + contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/contracts/" + contractId))
                .andExpect(status().isNotFound());
    }
}
