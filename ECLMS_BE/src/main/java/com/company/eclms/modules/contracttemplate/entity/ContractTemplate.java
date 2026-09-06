package com.company.eclms.modules.contracttemplate.entity;

import com.company.eclms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "contract_templates")
@SQLDelete(sql = "UPDATE contract_templates SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class ContractTemplate extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "variables")
    private String variables; // e.g. "vendor_name,amount,expiry_date"

    @Column(name = "version_string", nullable = false, length = 50)
    private String versionString = "1.0";

    @Column(name = "status", nullable = false, length = 50)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED
}
