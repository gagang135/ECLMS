package com.company.eclms.modules.permission.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.modules.permission.dto.PermissionDto;
import com.company.eclms.modules.permission.entity.Permission;
import com.company.eclms.modules.permission.mapper.PermissionMapper;
import com.company.eclms.modules.permission.repository.PermissionRepository;
import com.company.eclms.modules.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionDto createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsByName(permissionDto.getName())) {
            throw new ConflictException("Permission with name " + permissionDto.getName() + " already exists");
        }
        Permission permission = permissionMapper.toEntity(permissionDto);
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toDto(savedPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDto getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found with ID: " + id));
        return permissionMapper.toDto(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PermissionDto updatePermission(UUID id, PermissionDto permissionDto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found with ID: " + id));

        if (!permission.getName().equals(permissionDto.getName()) && 
                permissionRepository.existsByName(permissionDto.getName())) {
            throw new ConflictException("Permission with name " + permissionDto.getName() + " already exists");
        }

        permission.setName(permissionDto.getName());
        permission.setPermissionGroup(permissionDto.getPermissionGroup());
        permission.setDescription(permissionDto.getDescription());

        Permission updatedPermission = permissionRepository.save(permission);
        return permissionMapper.toDto(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found with ID: " + id));
        permissionRepository.delete(permission);
    }
}
