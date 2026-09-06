package com.company.eclms.modules.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {

    private UUID id;

    @NotBlank(message = "Contract name is required")
    private String name;

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;
    private String vendorName;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
    private String departmentName;

    private UUID templateId;
    private String templateName;

    private String content;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate renewalDate;

    private BigDecimal riskScore;

    private String riskAssessment;

    private String metadata;

    private String versionString;

    // Transient map to populate variables
    private Map<String, String> templateVariables;
}
