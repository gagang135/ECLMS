package com.company.eclms.modules.contracttemplate.service;

import com.company.eclms.modules.contracttemplate.dto.ContractTemplateDto;

import java.util.List;
import java.util.UUID;

public interface ContractTemplateService {
    ContractTemplateDto createTemplate(ContractTemplateDto contractTemplateDto);
    ContractTemplateDto getTemplateById(UUID id);
    List<ContractTemplateDto> getAllTemplates();
    ContractTemplateDto updateTemplate(UUID id, ContractTemplateDto contractTemplateDto);
    void deleteTemplate(UUID id);
    ContractTemplateDto publishTemplate(UUID id);
    ContractTemplateDto archiveTemplate(UUID id);
}
