package com.company.eclms.modules.role.service;

import com.company.eclms.modules.role.dto.RoleDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {
    RoleDto createRole(RoleDto roleDto);
    RoleDto getRoleById(UUID id);
    List<RoleDto> getAllRoles();
    RoleDto updateRole(UUID id, RoleDto roleDto);
    void deleteRole(UUID id);
    RoleDto assignPermissionsToRole(UUID roleId, Set<UUID> permissionIds);
}
