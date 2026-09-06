package com.company.eclms.modules.department.service;

import com.company.eclms.modules.department.dto.DepartmentDto;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    DepartmentDto getDepartmentById(UUID id);
    List<DepartmentDto> getDepartmentTree();
    List<DepartmentDto> getAllDepartments();
    DepartmentDto updateDepartment(UUID id, DepartmentDto departmentDto);
    void deleteDepartment(UUID id);
}
