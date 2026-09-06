package com.company.eclms.modules.contract.mapper;

import com.company.eclms.modules.contract.dto.ContractDto;
import com.company.eclms.modules.contract.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "template.id", target = "templateId")
    @Mapping(source = "template.name", target = "templateName")
    @Mapping(target = "templateVariables", ignore = true)
    ContractDto toDto(Contract contract);

    @Mapping(source = "vendorId", target = "vendor.id")
    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "templateId", target = "template.id")
    Contract toEntity(ContractDto contractDto);
}
