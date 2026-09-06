package com.company.eclms.modules.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDto {

    private UUID id;

    @NotBlank(message = "Workflow name is required")
    private String name;

    private String description;

    private String status;

    @NotEmpty(message = "Workflow must contain at least one step")
    @Valid
    private List<WorkflowStepDto> steps;
}
