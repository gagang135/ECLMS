package com.company.eclms.modules.vendor.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.vendor.dto.VendorDto;
import com.company.eclms.modules.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR_CREATE')")
    public ApiResponse<VendorDto> createVendor(@Valid @RequestBody VendorDto vendorDto) {
        VendorDto created = vendorService.createVendor(vendorDto);
        return ApiResponse.success(created, "Vendor created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_READ')")
    public ApiResponse<VendorDto> getVendorById(@PathVariable UUID id) {
        VendorDto vendorDto = vendorService.getVendorById(id);
        return ApiResponse.success(vendorDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_READ')")
    public ApiResponse<Page<VendorDto>> getVendors(
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<VendorDto> vendors = vendorService.getVendors(pageable, search);
        return ApiResponse.success(vendors);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ApiResponse<VendorDto> updateVendor(@PathVariable UUID id, @Valid @RequestBody VendorDto vendorDto) {
        VendorDto updated = vendorService.updateVendor(id, vendorDto);
        return ApiResponse.success(updated, "Vendor updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_DELETE')")
    public ApiResponse<Void> deleteVendor(@PathVariable UUID id) {
        vendorService.deleteVendor(id);
        return ApiResponse.success(null, "Vendor deleted successfully");
    }
}
