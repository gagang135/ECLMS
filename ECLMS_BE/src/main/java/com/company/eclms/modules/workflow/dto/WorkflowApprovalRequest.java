package com.company.eclms.modules.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowApprovalRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "Action must be APPROVED or REJECTED")
    private String action;

    private String comments;
}
