package com.company.eclms.modules.dashboard.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.dashboard.dto.DashboardDto;
import com.company.eclms.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    public ApiResponse<DashboardDto> getDashboardStats() {
        DashboardDto stats = dashboardService.getDashboardStats();
        return ApiResponse.success(stats);
    }
}
