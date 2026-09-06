package com.company.eclms.modules.vendor.service;

import com.company.eclms.modules.vendor.dto.VendorDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VendorService {
    VendorDto createVendor(VendorDto vendorDto);
    VendorDto getVendorById(UUID id);
    Page<VendorDto> getVendors(Pageable pageable, String search);
    VendorDto updateVendor(UUID id, VendorDto vendorDto);
    void deleteVendor(UUID id);
}
