package com.company.eclms.modules.permission.mapper;

import com.company.eclms.modules.permission.dto.PermissionDto;
import com.company.eclms.modules.permission.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toDto(Permission permission);
    Permission toEntity(PermissionDto permissionDto);
}
