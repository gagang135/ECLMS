package com.company.eclms.modules.user.mapper;

import com.company.eclms.modules.role.mapper.RoleMapper;
import com.company.eclms.modules.user.dto.UserDto;
import com.company.eclms.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")
    UserDto toDto(User user);

    @Mapping(source = "departmentId", target = "department.id")
    User toEntity(UserDto userDto);
}
