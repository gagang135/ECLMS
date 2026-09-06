package com.company.eclms.modules.vendor.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.vendor.dto.VendorDto;
import com.company.eclms.modules.vendor.repository.VendorRepository;
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

class VendorControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private VendorRepository vendorRepository;

    @BeforeEach
    void setUp() {
        vendorRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"VENDOR_CREATE", "VENDOR_READ", "VENDOR_UPDATE", "VENDOR_DELETE"})
    void vendorCrud_Success() throws Exception {
        // 1. Create Vendor
        VendorDto createDto = VendorDto.builder()
                .name("Acme Corp")
                .email("info@acme.com")
                .phone("1234567890")
                .address("123 Acme St")
                .riskLevel("LOW")
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/vendors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Acme Corp"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID vendorId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Vendor
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vendors/" + vendorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("info@acme.com"));

        // 3. Update Vendor
        VendorDto updateDto = VendorDto.builder()
                .name("Acme Corporation")
                .email("contact@acme.com")
                .phone("9876543210")
                .address("456 Acme Rd")
                .riskLevel("MEDIUM")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/vendors/" + vendorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Acme Corporation"))
                .andExpect(jsonPath("$.data.email").value("contact@acme.com"));

        // 4. Delete Vendor
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/vendors/" + vendorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vendors/" + vendorId))
                .andExpect(status().isNotFound());
    }
}
