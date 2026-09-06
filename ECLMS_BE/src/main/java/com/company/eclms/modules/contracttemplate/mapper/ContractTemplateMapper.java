package com.company.eclms.modules.contracttemplate.mapper;

import com.company.eclms.modules.contracttemplate.dto.ContractTemplateDto;
import com.company.eclms.modules.contracttemplate.entity.ContractTemplate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContractTemplateMapper {
    ContractTemplateDto toDto(ContractTemplate contractTemplate);
    ContractTemplate toEntity(ContractTemplateDto contractTemplateDto);
}
