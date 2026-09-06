package com.company.eclms.modules.contract.service;

import com.company.eclms.modules.contract.dto.ContractDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ContractService {
    ContractDto createContract(ContractDto contractDto);
    ContractDto getContractById(UUID id);
    Page<ContractDto> getContracts(Pageable pageable, String search, String status, UUID departmentId, UUID vendorId);
    ContractDto updateContract(UUID id, ContractDto contractDto);
    void deleteContract(UUID id);
    ContractDto renewContract(UUID id, LocalDate newEndDate);
    ContractDto terminateContract(UUID id);
}
