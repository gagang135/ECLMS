package com.company.eclms.modules.contract.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.contract.dto.ContractDto;
import com.company.eclms.modules.contract.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAuthority('CONTRACT_CREATE')")
    public ApiResponse<ContractDto> createContract(@Valid @RequestBody ContractDto dto) {
        ContractDto created = contractService.createContract(dto);
        return ApiResponse.success(created, "Contract created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_READ')")
    public ApiResponse<ContractDto> getContractById(@PathVariable UUID id) {
        ContractDto dto = contractService.getContractById(id);
        return ApiResponse.success(dto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_READ')")
    public ApiResponse<Page<ContractDto>> getContracts(
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID vendorId) {
        Page<ContractDto> contracts = contractService.getContracts(pageable, search, status, departmentId, vendorId);
        return ApiResponse.success(contracts);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    public ApiResponse<ContractDto> updateContract(@PathVariable UUID id, @Valid @RequestBody ContractDto dto) {
        ContractDto updated = contractService.updateContract(id, dto);
        return ApiResponse.success(updated, "Contract updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_DELETE')")
    public ApiResponse<Void> deleteContract(@PathVariable UUID id) {
        contractService.deleteContract(id);
        return ApiResponse.success(null, "Contract deleted successfully");
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAuthority('CONTRACT_RENEW')")
    public ApiResponse<ContractDto> renewContract(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate) {
        ContractDto renewed = contractService.renewContract(id, newEndDate);
        return ApiResponse.success(renewed, "Contract renewed successfully");
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('CONTRACT_TERMINATE')")
    public ApiResponse<ContractDto> terminateContract(@PathVariable UUID id) {
        ContractDto terminated = contractService.terminateContract(id);
        return ApiResponse.success(terminated, "Contract terminated successfully");
    }
}
