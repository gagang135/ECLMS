package com.company.eclms.modules.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceDto {
    private UUID id;
    private UUID workflowId;
    private String workflowName;
    private UUID contractId;
    private String contractName;
    private int currentStepNumber;
    private String status;
}
