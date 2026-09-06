package com.company.eclms.modules.workflow.dto;

import jakarta.validation.constraints.Min;
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
public class WorkflowStepDto {

    private UUID id;

    @Min(value = 1, message = "Step number must be at least 1")
    private int stepNumber;

    @NotBlank(message = "Step type is required")
    @Pattern(regexp = "^(SEQUENTIAL|PARALLEL)$", message = "Step type must be SEQUENTIAL or PARALLEL")
    private String stepType;

    private UUID assigneeRoleId;
    private String assigneeRoleName;

    private UUID assigneeUserId;
    private String assigneeUserName;

    @Min(value = 1, message = "Required approvals count must be at least 1")
    private int requiredApprovals = 1;
}
