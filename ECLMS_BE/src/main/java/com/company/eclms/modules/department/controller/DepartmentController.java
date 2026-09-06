package com.company.eclms.modules.department.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.department.dto.DepartmentDto;
import com.company.eclms.modules.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
    public ApiResponse<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        DepartmentDto created = departmentService.createDepartment(departmentDto);
        return ApiResponse.success(created, "Department created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ApiResponse<DepartmentDto> getDepartmentById(@PathVariable UUID id) {
        DepartmentDto departmentDto = departmentService.getDepartmentById(id);
        return ApiResponse.success(departmentDto);
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ApiResponse<List<DepartmentDto>> getDepartmentTree() {
        List<DepartmentDto> tree = departmentService.getDepartmentTree();
        return ApiResponse.success(tree);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ApiResponse<List<DepartmentDto>> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ApiResponse.success(departments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    public ApiResponse<DepartmentDto> updateDepartment(@PathVariable UUID id, @Valid @RequestBody DepartmentDto departmentDto) {
        DepartmentDto updated = departmentService.updateDepartment(id, departmentDto);
        return ApiResponse.success(updated, "Department updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    public ApiResponse<Void> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ApiResponse.success(null, "Department deleted successfully");
    }
}
