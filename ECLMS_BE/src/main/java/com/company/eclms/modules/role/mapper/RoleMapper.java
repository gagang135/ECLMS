package com.company.eclms.modules.role.mapper;

import com.company.eclms.modules.permission.mapper.PermissionMapper;
import com.company.eclms.modules.role.dto.RoleDto;
import com.company.eclms.modules.role.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    @Mapping(source = "parentRole.id", target = "parentRoleId")
    RoleDto toDto(Role role);

    @Mapping(source = "parentRoleId", target = "parentRole.id")
    Role toEntity(RoleDto roleDto);
}
