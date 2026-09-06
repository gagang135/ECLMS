package com.company.eclms.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private long totalContracts;
    private long totalVendors;
    private long totalDepartments;
    private Map<String, Long> contractsByStatus;
    private long contractsExpiringSoon; // Expires in next 30 days
}
