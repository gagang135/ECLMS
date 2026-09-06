package com.company.eclms.modules.department.mapper;

import com.company.eclms.modules.department.dto.DepartmentDto;
import com.company.eclms.modules.department.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "parentDepartment.id", target = "parentDepartmentId")
    DepartmentDto toDto(Department department);

    @Mapping(source = "parentDepartmentId", target = "parentDepartment.id")
    Department toEntity(DepartmentDto departmentDto);
}
