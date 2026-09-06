package com.company.eclms.modules.contracttemplate.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.contracttemplate.dto.ContractTemplateDto;
import com.company.eclms.modules.contracttemplate.service.ContractTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contract-templates")
@RequiredArgsConstructor
public class ContractTemplateController {

    private final ContractTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasAuthority('TEMPLATE_CREATE')")
    public ApiResponse<ContractTemplateDto> createTemplate(@Valid @RequestBody ContractTemplateDto dto) {
        ContractTemplateDto created = templateService.createTemplate(dto);
        return ApiResponse.success(created, "Template created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ApiResponse<ContractTemplateDto> getTemplateById(@PathVariable UUID id) {
        ContractTemplateDto dto = templateService.getTemplateById(id);
        return ApiResponse.success(dto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ApiResponse<List<ContractTemplateDto>> getAllTemplates() {
        List<ContractTemplateDto> templates = templateService.getAllTemplates();
        return ApiResponse.success(templates);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_UPDATE')")
    public ApiResponse<ContractTemplateDto> updateTemplate(@PathVariable UUID id, @Valid @RequestBody ContractTemplateDto dto) {
        ContractTemplateDto updated = templateService.updateTemplate(id, dto);
        return ApiResponse.success(updated, "Template updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_DELETE')")
    public ApiResponse<Void> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success(null, "Template deleted successfully");
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('TEMPLATE_PUBLISH')")
    public ApiResponse<ContractTemplateDto> publishTemplate(@PathVariable UUID id) {
        ContractTemplateDto published = templateService.publishTemplate(id);
        return ApiResponse.success(published, "Template published successfully");
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('TEMPLATE_PUBLISH')")
    public ApiResponse<ContractTemplateDto> archiveTemplate(@PathVariable UUID id) {
        ContractTemplateDto archived = templateService.archiveTemplate(id);
        return ApiResponse.success(archived, "Template archived successfully");
    }
}
