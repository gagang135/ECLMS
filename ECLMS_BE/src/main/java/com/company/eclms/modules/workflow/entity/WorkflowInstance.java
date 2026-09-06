package com.company.eclms.modules.workflow.entity;

import com.company.eclms.common.entity.BaseEntity;
import com.company.eclms.modules.contract.entity.Contract;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "workflow_instances")
@SQLDelete(sql = "UPDATE workflow_instances SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class WorkflowInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "current_step_number", nullable = false)
    private int currentStepNumber = 1;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, APPROVED, REJECTED

    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowApproval> approvals = new ArrayList<>();
}
