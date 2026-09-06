package com.company.eclms.modules.department.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.modules.department.dto.DepartmentDto;
import com.company.eclms.modules.department.entity.Department;
import com.company.eclms.modules.department.mapper.DepartmentMapper;
import com.company.eclms.modules.department.repository.DepartmentRepository;
import com.company.eclms.modules.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        Department department = departmentMapper.toEntity(departmentDto);
        
        if (departmentDto.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(departmentDto.getParentDepartmentId())
                    .orElseThrow(() -> new NotFoundException("Parent department not found"));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with ID: " + id));
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartmentTree() {
        List<Department> roots = departmentRepository.findRootDepartments();
        return roots.stream()
                .map(departmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(UUID id, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with ID: " + id));

        // Cycle check
        if (departmentDto.getParentDepartmentId() != null) {
            if (departmentDto.getParentDepartmentId().equals(id)) {
                throw new ConflictException("A department cannot be its own parent");
            }
            // Check if proposed parent is a child of this department to prevent cycle
            Department parent = departmentRepository.findById(departmentDto.getParentDepartmentId())
                    .orElseThrow(() -> new NotFoundException("Parent department not found"));
            
            Department temp = parent;
            while (temp != null) {
                if (temp.getId().equals(id)) {
                    throw new ConflictException("Circular department hierarchy detected");
                }
                temp = temp.getParentDepartment();
            }
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());

        Department updated = departmentRepository.save(department);
        return departmentMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with ID: " + id));
        departmentRepository.delete(department);
    }
}
