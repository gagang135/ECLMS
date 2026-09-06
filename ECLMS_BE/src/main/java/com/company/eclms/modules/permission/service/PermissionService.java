package com.company.eclms.modules.permission.service;

import com.company.eclms.modules.permission.dto.PermissionDto;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionDto createPermission(PermissionDto permissionDto);
    PermissionDto getPermissionById(UUID id);
    List<PermissionDto> getAllPermissions();
    PermissionDto updatePermission(UUID id, PermissionDto permissionDto);
    void deletePermission(UUID id);
}
