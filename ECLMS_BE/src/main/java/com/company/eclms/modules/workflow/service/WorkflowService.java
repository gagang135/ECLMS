package com.company.eclms.modules.workflow.service;

import com.company.eclms.modules.workflow.dto.*;

import java.util.List;
import java.util.UUID;

public interface WorkflowService {
    WorkflowDto createWorkflow(WorkflowDto workflowDto);
    WorkflowDto getWorkflowById(UUID id);
    List<WorkflowDto> getAllWorkflows();
    WorkflowInstanceDto startWorkflow(UUID workflowId, UUID contractId);
    WorkflowInstanceDto approveOrRejectStep(UUID instanceId, UUID userId, WorkflowApprovalRequest request);
    List<WorkflowApprovalDto> getApprovalHistory(UUID instanceId);
    List<WorkflowInstanceDto> getAllWorkflowInstances();
}
