package com.company.eclms.modules.contract.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.modules.contract.dto.ContractDto;
import com.company.eclms.modules.contract.entity.Contract;
import com.company.eclms.modules.contract.mapper.ContractMapper;
import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.contract.service.ContractService;
import com.company.eclms.modules.contracttemplate.entity.ContractTemplate;
import com.company.eclms.modules.contracttemplate.repository.ContractTemplateRepository;
import com.company.eclms.modules.department.entity.Department;
import com.company.eclms.modules.department.repository.DepartmentRepository;
import com.company.eclms.modules.vendor.entity.Vendor;
import com.company.eclms.modules.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final ContractTemplateRepository templateRepository;
    private final ContractMapper contractMapper;

    @Override
    @Transactional
    public ContractDto createContract(ContractDto dto) {
        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + dto.getVendorId()));
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        Contract contract = new Contract();
        contract.setName(dto.getName());
        contract.setVendor(vendor);
        contract.setDepartment(department);
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setRenewalDate(dto.getRenewalDate());
        contract.setRiskScore(dto.getRiskScore() != null ? dto.getRiskScore() : BigDecimal.ZERO);
        contract.setRiskAssessment(dto.getRiskAssessment());
        contract.setMetadata(dto.getMetadata());
        contract.setStatus("DRAFT");
        contract.setVersionString("1.0");

        if (dto.getTemplateId() != null) {
            ContractTemplate template = templateRepository.findById(dto.getTemplateId())
                    .orElseThrow(() -> new NotFoundException("Template not found with ID: " + dto.getTemplateId()));
            contract.setTemplate(template);
            
            // Interpolate content
            String interpolated = interpolate(template.getContent(), dto.getTemplateVariables());
            contract.setContent(interpolated);
        } else {
            contract.setContent(dto.getContent());
        }

        Contract saved = contractRepository.save(contract);
        return contractMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDto getContractById(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + id));
        return contractMapper.toDto(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractDto> getContracts(Pageable pageable, String search, String status, UUID departmentId, UUID vendorId) {
        Specification<Contract> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            if (vendorId != null) {
                predicates.add(cb.equal(root.get("vendor").get("id"), vendorId));
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("content")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return contractRepository.findAll(spec, pageable).map(contractMapper::toDto);
    }

    @Override
    @Transactional
    public ContractDto updateContract(UUID id, ContractDto dto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + id));

        if (!"DRAFT".equals(contract.getStatus()) && !"REJECTED".equals(contract.getStatus())) {
            throw new ConflictException("Contract can only be updated if it is in DRAFT or REJECTED status");
        }

        contract.setName(dto.getName());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setRenewalDate(dto.getRenewalDate());
        contract.setRiskScore(dto.getRiskScore() != null ? dto.getRiskScore() : BigDecimal.ZERO);
        contract.setRiskAssessment(dto.getRiskAssessment());
        contract.setMetadata(dto.getMetadata());

        if (dto.getTemplateId() != null) {
            ContractTemplate template = templateRepository.findById(dto.getTemplateId())
                    .orElseThrow(() -> new NotFoundException("Template not found with ID: " + dto.getTemplateId()));
            contract.setTemplate(template);
            String interpolated = interpolate(template.getContent(), dto.getTemplateVariables());
            contract.setContent(interpolated);
        } else {
            contract.setContent(dto.getContent());
        }

        Contract updated = contractRepository.save(contract);
        return contractMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteContract(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + id));
        contractRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public ContractDto renewContract(UUID id, LocalDate newEndDate) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + id));

        if (!"ACTIVE".equals(contract.getStatus()) && !"EXPIRED".equals(contract.getStatus())) {
            throw new ConflictException("Only ACTIVE or EXPIRED contracts can be renewed");
        }

        // Increment version
        double currentVersion = Double.parseDouble(contract.getVersionString());
        contract.setVersionString(String.format("%.1f", currentVersion + 1.0));
        
        contract.setEndDate(newEndDate);
        contract.setRenewalDate(newEndDate.minusMonths(1)); // alert 1 month before expiry
        contract.setStatus("DRAFT"); // goes back to draft for approval

        Contract saved = contractRepository.save(contract);
        return contractMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ContractDto terminateContract(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + id));

        contract.setStatus("TERMINATED");
        Contract saved = contractRepository.save(contract);
        return contractMapper.toDto(saved);
    }

    private String interpolate(String content, Map<String, String> variables) {
        if (content == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return content;
        }
        String result = content;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
