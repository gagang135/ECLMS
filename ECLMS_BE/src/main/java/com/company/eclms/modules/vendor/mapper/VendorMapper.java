package com.company.eclms.modules.vendor.mapper;

import com.company.eclms.modules.vendor.dto.VendorDto;
import com.company.eclms.modules.vendor.entity.Vendor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VendorMapper {
    VendorDto toDto(Vendor vendor);
    Vendor toEntity(VendorDto vendorDto);
}
