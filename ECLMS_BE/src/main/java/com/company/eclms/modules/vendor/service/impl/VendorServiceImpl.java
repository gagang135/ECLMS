package com.company.eclms.modules.vendor.service.impl;

import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.modules.vendor.dto.VendorDto;
import com.company.eclms.modules.vendor.entity.Vendor;
import com.company.eclms.modules.vendor.mapper.VendorMapper;
import com.company.eclms.modules.vendor.repository.VendorRepository;
import com.company.eclms.modules.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    @Override
    @Transactional
    public VendorDto createVendor(VendorDto vendorDto) {
        Vendor vendor = vendorMapper.toEntity(vendorDto);
        if (vendor.getRiskLevel() == null) {
            vendor.setRiskLevel("LOW");
        }
        Vendor saved = vendorRepository.save(vendor);
        return vendorMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDto getVendorById(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + id));
        return vendorMapper.toDto(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorDto> getVendors(Pageable pageable, String search) {
        Specification<Vendor> spec = (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("gstNumber")), pattern),
                cb.like(cb.lower(root.get("panNumber")), pattern)
            );
        };
        return vendorRepository.findAll(spec, pageable).map(vendorMapper::toDto);
    }

    @Override
    @Transactional
    public VendorDto updateVendor(UUID id, VendorDto vendorDto) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + id));

        vendor.setName(vendorDto.getName());
        vendor.setEmail(vendorDto.getEmail());
        vendor.setPhone(vendorDto.getPhone());
        vendor.setAddress(vendorDto.getAddress());
        vendor.setGstNumber(vendorDto.getGstNumber());
        vendor.setPanNumber(vendorDto.getPanNumber());
        vendor.setRiskLevel(vendorDto.getRiskLevel() != null ? vendorDto.getRiskLevel() : "LOW");

        Vendor updated = vendorRepository.save(vendor);
        return vendorMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteVendor(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + id));
        vendorRepository.delete(vendor);
    }
}
