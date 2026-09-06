package com.company.eclms.modules.role.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.modules.permission.entity.Permission;
import com.company.eclms.modules.permission.repository.PermissionRepository;
import com.company.eclms.modules.role.dto.RoleDto;
import com.company.eclms.modules.role.entity.Role;
import com.company.eclms.modules.role.mapper.RoleMapper;
import com.company.eclms.modules.role.repository.RoleRepository;
import com.company.eclms.modules.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new ConflictException("Role with name " + roleDto.getName() + " already exists");
        }
        Role role = roleMapper.toEntity(roleDto);
        
        if (roleDto.getParentRoleId() != null) {
            Role parent = roleRepository.findById(roleDto.getParentRoleId())
                    .orElseThrow(() -> new NotFoundException("Parent role not found with ID: " + roleDto.getParentRoleId()));
            role.setParentRole(parent);
        } else {
            role.setParentRole(null);
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + id));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDto updateRole(UUID id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + id));

        if (!role.getName().equals(roleDto.getName()) && roleRepository.existsByName(roleDto.getName())) {
            throw new ConflictException("Role with name " + roleDto.getName() + " already exists");
        }

        // Circular hierarchy check
        if (roleDto.getParentRoleId() != null) {
            if (roleDto.getParentRoleId().equals(id)) {
                throw new ConflictException("A role cannot be its own parent");
            }
            Role parent = roleRepository.findById(roleDto.getParentRoleId())
                    .orElseThrow(() -> new NotFoundException("Parent role not found with ID: " + roleDto.getParentRoleId()));
            role.setParentRole(parent);
        } else {
            role.setParentRole(null);
        }

        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toDto(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + id));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDto assignPermissionsToRole(UUID roleId, Set<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(new HashSet<>(permissions));

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toDto(updatedRole);
    }
}
