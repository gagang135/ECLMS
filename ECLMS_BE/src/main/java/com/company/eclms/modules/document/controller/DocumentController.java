package com.company.eclms.modules.document.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.document.dto.DocumentDto;
import com.company.eclms.modules.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    public ApiResponse<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "contractId", required = false) UUID contractId) {
        try {
            DocumentDto dto = documentService.uploadDocument(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream(),
                    file.getSize(),
                    contractId
            );
            return ApiResponse.success(dto, "Document uploaded successfully");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "Document upload failed");
        }
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable UUID id) {
        InputStream is = documentService.downloadDocumentContent(id);
        DocumentDto dto = documentService.getDocumentById(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(dto.getFileType()))
                .body(new InputStreamResource(is));
    }

    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ApiResponse<String> getPreviewUrl(@PathVariable UUID id) {
        String url = documentService.getDocumentPreviewUrl(id);
        return ApiResponse.success(url);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ApiResponse<DocumentDto> getDocumentById(@PathVariable UUID id) {
        DocumentDto dto = documentService.getDocumentById(id);
        return ApiResponse.success(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ApiResponse<Void> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ApiResponse.success(null, "Document deleted successfully");
    }

    @GetMapping("/contract/{contractId}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ApiResponse<List<DocumentDto>> getDocumentsByContract(@PathVariable UUID contractId) {
        List<DocumentDto> dtos = documentService.getDocumentsByContract(contractId);
        return ApiResponse.success(dtos);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ApiResponse<List<DocumentDto>> getAllDocuments() {
        List<DocumentDto> dtos = documentService.getAllDocuments();
        return ApiResponse.success(dtos);
    }
}
