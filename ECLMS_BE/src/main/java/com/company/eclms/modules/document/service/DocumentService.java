package com.company.eclms.modules.document.service;

import com.company.eclms.modules.document.dto.DocumentDto;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentDto uploadDocument(String fileName, String contentType, InputStream inputStream, long size, UUID contractId);
    InputStream downloadDocumentContent(UUID documentId);
    DocumentDto getDocumentById(UUID id);
    String getDocumentPreviewUrl(UUID id);
    void deleteDocument(UUID id);
    DocumentDto updateOcrText(UUID id, String ocrText);
    List<DocumentDto> getDocumentsByContract(UUID contractId);
    List<DocumentDto> getAllDocuments();
}
