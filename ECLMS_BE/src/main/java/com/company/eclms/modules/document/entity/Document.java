package com.company.eclms.modules.document.entity;

import com.company.eclms.common.entity.BaseEntity;
import com.company.eclms.modules.contract.entity.Contract;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "documents")
@SQLDelete(sql = "UPDATE documents SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class Document extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    @Column(name = "doc_size", nullable = false)
    private long docSize;

    @Column(name = "version_number", nullable = false)
    private int versionNumber = 1;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for attributes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;
}
