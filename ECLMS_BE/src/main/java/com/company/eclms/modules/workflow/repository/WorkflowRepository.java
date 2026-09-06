package com.company.eclms.modules.workflow.repository;

import com.company.eclms.modules.workflow.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID>, JpaSpecificationExecutor<Workflow> {
    Optional<Workflow> findByName(String name);
}
