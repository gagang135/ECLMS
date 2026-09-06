package com.company.eclms.modules.department.repository;

import com.company.eclms.modules.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {
    
    @Query("SELECT d FROM Department d WHERE d.parentDepartment IS NULL")
    List<Department> findRootDepartments();
}
