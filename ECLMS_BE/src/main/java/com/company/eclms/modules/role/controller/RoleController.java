package com.company.eclms.modules.role.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.role.dto.RoleDto;
import com.company.eclms.modules.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ApiResponse<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        RoleDto created = roleService.createRole(roleDto);
        return ApiResponse.success(created, "Role created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleDto> getRoleById(@PathVariable UUID id) {
        RoleDto roleDto = roleService.getRoleById(id);
        return ApiResponse.success(roleDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ApiResponse.success(roles);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<RoleDto> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleDto roleDto) {
        RoleDto updated = roleService.updateRole(id, roleDto);
        return ApiResponse.success(updated, "Role updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ApiResponse<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null, "Role deleted successfully");
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN_PERMISSION')")
    public ApiResponse<RoleDto> assignPermissions(
            @PathVariable UUID id,
            @RequestBody Set<UUID> permissionIds) {
        RoleDto roleDto = roleService.assignPermissionsToRole(id, permissionIds);
        return ApiResponse.success(roleDto, "Permissions assigned to role successfully");
    }
}
