package com.company.eclms.modules.workflow.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.common.security.CustomUserDetails;
import com.company.eclms.modules.workflow.dto.*;
import com.company.eclms.modules.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    @PreAuthorize("hasAuthority('WORKFLOW_CREATE')")
    public ApiResponse<WorkflowDto> createWorkflow(@Valid @RequestBody WorkflowDto workflowDto) {
        WorkflowDto created = workflowService.createWorkflow(workflowDto);
        return ApiResponse.success(created, "Workflow template created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ApiResponse<WorkflowDto> getWorkflowById(@PathVariable UUID id) {
        WorkflowDto workflowDto = workflowService.getWorkflowById(id);
        return ApiResponse.success(workflowDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ApiResponse<List<WorkflowDto>> getAllWorkflows() {
        List<WorkflowDto> workflows = workflowService.getAllWorkflows();
        return ApiResponse.success(workflows);
    }

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('WORKFLOW_START')")
    public ApiResponse<WorkflowInstanceDto> startWorkflow(
            @RequestParam UUID workflowId,
            @RequestParam UUID contractId) {
        WorkflowInstanceDto instance = workflowService.startWorkflow(workflowId, contractId);
        return ApiResponse.success(instance, "Workflow instance started successfully");
    }

    @PostMapping("/instances/{instanceId}/action")
    @PreAuthorize("hasAuthority('WORKFLOW_APPROVE')")
    public ApiResponse<WorkflowInstanceDto> approveOrRejectStep(
            @PathVariable UUID instanceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WorkflowApprovalRequest request) {
        WorkflowInstanceDto instance = workflowService.approveOrRejectStep(instanceId, userDetails.getId(), request);
        return ApiResponse.success(instance, "Workflow action recorded successfully");
    }

    @GetMapping("/instances/{instanceId}/history")
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ApiResponse<List<WorkflowApprovalDto>> getApprovalHistory(@PathVariable UUID instanceId) {
        List<WorkflowApprovalDto> history = workflowService.getApprovalHistory(instanceId);
        return ApiResponse.success(history);
    }

    @GetMapping("/instances/list")
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ApiResponse<List<WorkflowInstanceDto>> getAllWorkflowInstances() {
        List<WorkflowInstanceDto> instances = workflowService.getAllWorkflowInstances();
        return ApiResponse.success(instances);
    }
}
