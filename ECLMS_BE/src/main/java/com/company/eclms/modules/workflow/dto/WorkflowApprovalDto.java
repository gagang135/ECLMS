package com.company.eclms.modules.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowApprovalDto {
    private UUID id;
    private UUID workflowInstanceId;
    private int stepNumber;
    private UUID userId;
    private String userName;
    private String status;
    private String comments;
    private LocalDateTime actionDate;
}
