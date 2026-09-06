package com.company.eclms.modules.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private UUID id;
    private String fileName;
    private String fileType;
    private long docSize;
    private String checksum;
    private int versionNumber;
    private UUID contractId;
    private String previewUrl;
    private String metadata;
    private String ocrText;
}
