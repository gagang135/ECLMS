package com.company.eclms.modules.contracttemplate.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.modules.contracttemplate.dto.ContractTemplateDto;
import com.company.eclms.modules.contracttemplate.entity.ContractTemplate;
import com.company.eclms.modules.contracttemplate.mapper.ContractTemplateMapper;
import com.company.eclms.modules.contracttemplate.repository.ContractTemplateRepository;
import com.company.eclms.modules.contracttemplate.service.ContractTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractTemplateServiceImpl implements ContractTemplateService {

    private final ContractTemplateRepository templateRepository;
    private final ContractTemplateMapper templateMapper;

    @Override
    @Transactional
    public ContractTemplateDto createTemplate(ContractTemplateDto dto) {
        ContractTemplate template = templateMapper.toEntity(dto);
        template.setStatus("DRAFT");
        template.setVersionString("1.0");
        
        // Auto-extract template variables from content
        template.setVariables(extractVariables(dto.getContent()));

        ContractTemplate saved = templateRepository.save(template);
        return templateMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractTemplateDto getTemplateById(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract Template not found with ID: " + id));
        return templateMapper.toDto(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractTemplateDto> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(templateMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractTemplateDto updateTemplate(UUID id, ContractTemplateDto dto) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract Template not found with ID: " + id));

        if ("PUBLISHED".equals(template.getStatus())) {
            // Version increment if updating a published template (force a new copy or incremental edit)
            double currentVersion = Double.parseDouble(template.getVersionString());
            template.setVersionString(String.format("%.1f", currentVersion + 0.1));
            template.setStatus("DRAFT"); // Return to draft after version bump
        }

        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setContent(dto.getContent());
        template.setVariables(extractVariables(dto.getContent()));

        ContractTemplate updated = templateRepository.save(template);
        return templateMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract Template not found with ID: " + id));
        templateRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public ContractTemplateDto publishTemplate(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract Template not found with ID: " + id));

        if ("PUBLISHED".equals(template.getStatus())) {
            throw new ConflictException("Template is already published");
        }

        template.setStatus("PUBLISHED");
        ContractTemplate updated = templateRepository.save(template);
        return templateMapper.toDto(updated);
    }

    @Override
    @Transactional
    public ContractTemplateDto archiveTemplate(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract Template not found with ID: " + id));

        template.setStatus("ARCHIVED");
        ContractTemplate updated = templateRepository.save(template);
        return templateMapper.toDto(updated);
    }

    private String extractVariables(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        return String.join(",", variables);
    }
}
