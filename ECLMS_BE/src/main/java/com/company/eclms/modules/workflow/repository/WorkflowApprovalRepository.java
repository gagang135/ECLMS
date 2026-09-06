package com.company.eclms.modules.workflow.repository;

import com.company.eclms.modules.workflow.entity.WorkflowApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowApprovalRepository extends JpaRepository<WorkflowApproval, UUID> {
    List<WorkflowApproval> findByWorkflowInstanceIdOrderByActionDateDesc(UUID instanceId);
}
