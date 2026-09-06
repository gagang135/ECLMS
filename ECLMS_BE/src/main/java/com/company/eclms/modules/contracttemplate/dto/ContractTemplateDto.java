package com.company.eclms.modules.contracttemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTemplateDto {

    private UUID id;

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotBlank(message = "Template content is required")
    private String content;

    private String variables; // Comma-separated variable names

    private String versionString;

    @Pattern(regexp = "^(DRAFT|PUBLISHED|ARCHIVED)$", 
             message = "Status must be DRAFT, PUBLISHED, or ARCHIVED")
    private String status;
}
