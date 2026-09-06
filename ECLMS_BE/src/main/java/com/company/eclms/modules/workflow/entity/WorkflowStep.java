package com.company.eclms.modules.workflow.entity;

import com.company.eclms.common.entity.BaseEntity;
import com.company.eclms.modules.role.entity.Role;
import com.company.eclms.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "workflow_steps")
@SQLDelete(sql = "UPDATE workflow_steps SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class WorkflowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(name = "step_type", nullable = false, length = 50)
    private String stepType = "SEQUENTIAL"; // SEQUENTIAL, PARALLEL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_role_id")
    private Role assigneeRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private User assigneeUser;

    @Column(name = "required_approvals", nullable = false)
    private int requiredApprovals = 1;
}
