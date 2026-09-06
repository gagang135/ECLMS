package com.company.eclms.modules.workflow.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.common.exception.WorkflowException;
import com.company.eclms.modules.contract.entity.Contract;
import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.user.entity.User;
import com.company.eclms.modules.user.repository.UserRepository;
import com.company.eclms.modules.workflow.dto.*;
import com.company.eclms.modules.workflow.entity.*;
import com.company.eclms.modules.workflow.mapper.WorkflowApprovalMapper;
import com.company.eclms.modules.workflow.mapper.WorkflowInstanceMapper;
import com.company.eclms.modules.workflow.mapper.WorkflowMapper;
import com.company.eclms.modules.workflow.repository.*;
import com.company.eclms.modules.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowApprovalRepository workflowApprovalRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final WorkflowMapper workflowMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowApprovalMapper workflowApprovalMapper;

    @Override
    @Transactional
    public WorkflowDto createWorkflow(WorkflowDto dto) {
        Workflow workflow = workflowMapper.toEntity(dto);
        workflow.setStatus("ACTIVE");

        // Set back-reference on steps
        if (workflow.getSteps() != null) {
            for (WorkflowStep step : workflow.getSteps()) {
                step.setWorkflow(workflow);
                if (step.getAssigneeRole() != null && step.getAssigneeRole().getId() == null) {
                    step.setAssigneeRole(null);
                }
                if (step.getAssigneeUser() != null && step.getAssigneeUser().getId() == null) {
                    step.setAssigneeUser(null);
                }
            }
        }

        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDto getWorkflowById(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workflow template not found with ID: " + id));
        return workflowMapper.toDto(workflow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDto> getAllWorkflows() {
        return workflowRepository.findAll().stream()
                .map(workflowMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkflowInstanceDto startWorkflow(UUID workflowId, UUID contractId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found with ID: " + workflowId));
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + contractId));

        // Check if there is already an active instance
        workflowInstanceRepository.findByContractIdAndStatus(contractId, "IN_PROGRESS")
                .ifPresent(instance -> {
                    throw new ConflictException("Contract already has an active approval workflow in progress");
                });

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflow(workflow);
        instance.setContract(contract);
        instance.setCurrentStepNumber(1);
        instance.setStatus("IN_PROGRESS");

        contract.setStatus("IN_REVIEW");
        contractRepository.save(contract);

        WorkflowInstance saved = workflowInstanceRepository.save(instance);
        return workflowInstanceMapper.toDto(saved);
    }

    @Override
    @Transactional
    public WorkflowInstanceDto approveOrRejectStep(UUID instanceId, UUID userId, WorkflowApprovalRequest request) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new NotFoundException("Workflow instance not found with ID: " + instanceId));

        if (!"IN_PROGRESS".equals(instance.getStatus())) {
            throw new ConflictException("Cannot take action on a closed workflow instance. Current status: " + instance.getStatus());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Get current step details
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepNumberAsc(instance.getWorkflow().getId());
        int currentStepNum = instance.getCurrentStepNumber();
        
        WorkflowStep currentStep = steps.stream()
                .filter(step -> step.getStepNumber() == currentStepNum)
                .findFirst()
                .orElseThrow(() -> new WorkflowException("Step configuration not found for index: " + currentStepNum));

        // Validate permissions
        boolean isAuthorized = false;
        if (currentStep.getAssigneeUser() != null && currentStep.getAssigneeUser().getId().equals(userId)) {
            isAuthorized = true;
        } else if (currentStep.getAssigneeRole() != null) {
            boolean hasRole = user.getRoles().stream()
                    .anyMatch(role -> role.getId().equals(currentStep.getAssigneeRole().getId()));
            if (hasRole) {
                isAuthorized = true;
            }
        }

        // Admin override permission
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        if (isAdmin) {
            isAuthorized = true;
        }

        if (!isAuthorized) {
            throw new WorkflowException("User is not authorized to approve/reject this workflow step");
        }

        // Add Approval Record
        WorkflowApproval approval = new WorkflowApproval();
        approval.setWorkflowInstance(instance);
        approval.setStepNumber(currentStepNum);
        approval.setUser(user);
        approval.setStatus(request.getAction());
        approval.setComments(request.getComments());
        approval.setActionDate(LocalDateTime.now());
        WorkflowApproval savedApproval = workflowApprovalRepository.save(approval);
        
        // Sync bidirectional relationship
        if (instance.getApprovals() != null) {
            instance.getApprovals().add(savedApproval);
        }

        Contract contract = instance.getContract();

        if ("REJECTED".equals(request.getAction())) {
            instance.setStatus("REJECTED");
            contract.setStatus("REJECTED");
            contractRepository.save(contract);
        } else if ("APPROVED".equals(request.getAction())) {
            // Count total approvals for this step
            long approvalCount = instance.getApprovals().stream()
                    .filter(app -> app.getStepNumber() == currentStepNum && "APPROVED".equals(app.getStatus()))
                    .count(); // already contains current approval since we added it to the list

            if (approvalCount >= currentStep.getRequiredApprovals()) {
                // Advance to next step
                int totalSteps = steps.size();
                if (currentStepNum < totalSteps) {
                    instance.setCurrentStepNumber(currentStepNum + 1);
                } else {
                    // Final step approved -> entire workflow completes
                    instance.setStatus("APPROVED");
                    contract.setStatus("ACTIVE");
                    contractRepository.save(contract);
                }
            }
        }

        WorkflowInstance updated = workflowInstanceRepository.save(instance);
        return workflowInstanceMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowApprovalDto> getApprovalHistory(UUID instanceId) {
        return workflowApprovalRepository.findByWorkflowInstanceIdOrderByActionDateDesc(instanceId).stream()
                .map(workflowApprovalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto> getAllWorkflowInstances() {
        return workflowInstanceRepository.findAll().stream()
                .map(workflowInstanceMapper::toDto)
                .collect(Collectors.toList());
    }
}
