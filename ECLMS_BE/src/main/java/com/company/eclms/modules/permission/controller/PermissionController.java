package com.company.eclms.modules.permission.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.permission.dto.PermissionDto;
import com.company.eclms.modules.permission.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ApiResponse<PermissionDto> createPermission(@Valid @RequestBody PermissionDto permissionDto) {
        PermissionDto created = permissionService.createPermission(permissionDto);
        return ApiResponse.success(created, "Permission created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ApiResponse<PermissionDto> getPermissionById(@PathVariable UUID id) {
        PermissionDto permissionDto = permissionService.getPermissionById(id);
        return ApiResponse.success(permissionDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ApiResponse<List<PermissionDto>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.getAllPermissions();
        return ApiResponse.success(permissions);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ApiResponse<PermissionDto> updatePermission(@PathVariable UUID id, @Valid @RequestBody PermissionDto permissionDto) {
        PermissionDto updated = permissionService.updatePermission(id, permissionDto);
        return ApiResponse.success(updated, "Permission updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ApiResponse<Void> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ApiResponse.success(null, "Permission deleted successfully");
    }
}
