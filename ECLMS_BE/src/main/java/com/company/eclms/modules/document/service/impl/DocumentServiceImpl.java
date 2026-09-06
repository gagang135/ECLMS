package com.company.eclms.modules.document.service.impl;

import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.common.storage.StorageService;
import com.company.eclms.modules.contract.entity.Contract;
import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.document.dto.DocumentDto;
import com.company.eclms.modules.document.entity.Document;
import com.company.eclms.modules.document.mapper.DocumentMapper;
import com.company.eclms.modules.document.repository.DocumentRepository;
import com.company.eclms.modules.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ContractRepository contractRepository;
    private final DocumentMapper documentMapper;
    private final StorageService storageService;

    @Override
    @Transactional
    public DocumentDto uploadDocument(String fileName, String contentType, InputStream inputStream, long size, UUID contractId) {
        Contract contract = null;
        if (contractId != null) {
            contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new NotFoundException("Contract not found with ID: " + contractId));
        }

        try {
            // Read content into bytes to compute checksum and allow repeat reads
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            byte[] fileBytes = bos.toByteArray();

            // Calculate SHA-256 checksum
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String checksum = hexString.toString();

            // Upload using storage service
            String objectName = UUID.randomUUID() + "_" + fileName;
            String uploadedPath = storageService.uploadFile(
                    null, objectName, new ByteArrayInputStream(fileBytes), fileBytes.length, contentType
            );

            Document document = new Document();
            document.setFileName(fileName);
            document.setFilePath(objectName);
            document.setFileType(contentType);
            document.setChecksum(checksum);
            document.setDocSize(fileBytes.length);
            document.setVersionNumber(1);
            document.setContract(contract);

            Document saved = documentRepository.save(document);
            DocumentDto dto = documentMapper.toDto(saved);
            dto.setPreviewUrl(storageService.getPreviewUrl(null, objectName));
            return dto;

        } catch (Exception e) {
            log.error("Failed to process document upload", e);
            throw new RuntimeException("Document upload execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream downloadDocumentContent(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + documentId));
        return storageService.downloadFile(null, document.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getDocumentById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + id));
        DocumentDto dto = documentMapper.toDto(document);
        dto.setPreviewUrl(storageService.getPreviewUrl(null, document.getFilePath()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentPreviewUrl(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + id));
        return storageService.getPreviewUrl(null, document.getFilePath());
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + id));
        storageService.deleteFile(null, document.getFilePath());
        documentRepository.delete(document);
    }

    @Override
    @Transactional
    public DocumentDto updateOcrText(UUID id, String ocrText) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + id));
        document.setOcrText(ocrText);
        Document saved = documentRepository.save(document);
        return documentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByContract(UUID contractId) {
        return documentRepository.findByContractId(contractId).stream()
                .map(doc -> {
                    DocumentDto dto = documentMapper.toDto(doc);
                    dto.setPreviewUrl(storageService.getPreviewUrl(null, doc.getFilePath()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(doc -> {
                    DocumentDto dto = documentMapper.toDto(doc);
                    dto.setPreviewUrl(storageService.getPreviewUrl(null, doc.getFilePath()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
