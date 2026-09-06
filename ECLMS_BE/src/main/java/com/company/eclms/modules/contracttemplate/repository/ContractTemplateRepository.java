package com.company.eclms.modules.contracttemplate.repository;

import com.company.eclms.modules.contracttemplate.entity.ContractTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, UUID>, JpaSpecificationExecutor<ContractTemplate> {
    @Modifying
    @Query("UPDATE ContractTemplate t SET t.deleted = true, t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
