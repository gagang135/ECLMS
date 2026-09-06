package com.company.eclms.modules.dashboard.service.impl;

import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.department.repository.DepartmentRepository;
import com.company.eclms.modules.vendor.repository.VendorRepository;
import com.company.eclms.modules.dashboard.dto.DashboardDto;
import com.company.eclms.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboardStats() {
        long totalContracts = contractRepository.count();
        long totalVendors = vendorRepository.count();
        long totalDepartments = departmentRepository.count();

        // Get contracts counts by status
        Map<String, Long> statusCounts = new HashMap<>();
        jdbcTemplate.query("SELECT status, COUNT(*) as cnt FROM contracts WHERE deleted = false GROUP BY status", rs -> {
            statusCounts.put(rs.getString("status"), rs.getLong("cnt"));
        });

        // Get contracts expiring in next 30 days
        LocalDate futureDate = LocalDate.now().plusDays(30);
        Long expiringSoon = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM contracts WHERE deleted = false AND status = 'ACTIVE' AND end_date <= ?",
                Long.class,
                Date.valueOf(futureDate)
        );

        return DashboardDto.builder()
                .totalContracts(totalContracts)
                .totalVendors(totalVendors)
                .totalDepartments(totalDepartments)
                .contractsByStatus(statusCounts)
                .contractsExpiringSoon(expiringSoon != null ? expiringSoon : 0)
                .build();
    }
}
