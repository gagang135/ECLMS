package com.company.eclms.common.config;

import com.company.eclms.modules.permission.entity.Permission;
import com.company.eclms.modules.permission.repository.PermissionRepository;
import com.company.eclms.modules.role.entity.Role;
import com.company.eclms.modules.role.repository.RoleRepository;
import com.company.eclms.modules.user.entity.User;
import com.company.eclms.modules.user.repository.UserRepository;
import com.company.eclms.modules.workflow.entity.Workflow;
import com.company.eclms.modules.workflow.entity.WorkflowStep;
import com.company.eclms.modules.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowApprovalRepository workflowApprovalRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting database seeding...");



        // 1. Seed Permissions
        List<String> permissionNames = List.of(
                "USER_READ", "USER_UPDATE", "USER_DELETE", "USER_LOCK", "USER_ASSIGN_ROLE", "USER_ASSIGN_DEPARTMENT",
                "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE", "ROLE_ASSIGN_PERMISSION",
                "DEPARTMENT_CREATE", "DEPARTMENT_READ", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                "VENDOR_CREATE", "VENDOR_READ", "VENDOR_UPDATE", "VENDOR_DELETE",
                "TEMPLATE_CREATE", "TEMPLATE_READ", "TEMPLATE_UPDATE", "TEMPLATE_DELETE", "TEMPLATE_PUBLISH",
                "CONTRACT_CREATE", "CONTRACT_READ", "CONTRACT_UPDATE", "CONTRACT_DELETE", "CONTRACT_RENEW", "CONTRACT_TERMINATE",
                "DOCUMENT_UPLOAD", "DOCUMENT_READ", "DOCUMENT_DELETE",
                "WORKFLOW_CREATE", "WORKFLOW_READ", "WORKFLOW_START", "WORKFLOW_APPROVE",
                "PERMISSION_CREATE", "PERMISSION_READ", "PERMISSION_UPDATE", "PERMISSION_DELETE",
                "REPORT_EXPORT", "DASHBOARD_READ"
        );

        Set<Permission> allPermissions = new HashSet<>();
        for (String permName : permissionNames) {
            Permission permission = permissionRepository.findByName(permName)
                     .orElseGet(() -> {
                         Permission p = new Permission();
                         p.setName(permName);
                         p.setPermissionGroup(permName.split("_")[0]);
                         p.setDescription("Allows " + permName.replace("_", " ").toLowerCase());
                         p.setCreatedAt(LocalDateTime.now());
                         p.setCreatedBy("SYSTEM");
                         return permissionRepository.save(p);
                     });
            allPermissions.add(permission);
        }

        // 2. Seed Roles
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ADMIN");
                    r.setDescription("Administrator Role");
                    r.setPermissions(allPermissions);
                    r.setCreatedAt(LocalDateTime.now());
                    r.setCreatedBy("SYSTEM");
                    return roleRepository.save(r);
                });

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("USER");
                    r.setDescription("Standard User Role");
                    r.setPermissions(allPermissions); // Give all permissions for local CRUD test usage
                    r.setCreatedAt(LocalDateTime.now());
                    r.setCreatedBy("SYSTEM");
                    return roleRepository.save(r);
                });

        // 3. Seed Default Admin User & Reset Password/Roles
        User admin = userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("admin");
                    u.setEmail("admin@eclms.com");
                    u.setFullName("Default Admin");
                    u.setStatus("ACTIVE");
                    u.setCreatedAt(LocalDateTime.now());
                    u.setCreatedBy("SYSTEM");
                    return u;
                });
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(adminRole, userRole));
        userRepository.save(admin);
        log.info("Default admin user reset/seeded successfully (username: admin, password: admin123)");
        // 4. Seed Workflows
        if (workflowRepository.count() == 0) {
            String[] names = {
                "Standard Vendor Approval Workflow",
                "Non Disclosure Approval Workflow",
                "Software License Approval Workflow",
                "Consulting Agreement Approval Workflow"
            };

            for (String name : names) {
                Workflow workflow = new Workflow();
                workflow.setName(name);
                workflow.setDescription("A two-step sequential review process for " + name);
                workflow.setStatus("ACTIVE");
                workflow.setCreatedAt(LocalDateTime.now());
                workflow.setCreatedBy("SYSTEM");

                WorkflowStep step1 = new WorkflowStep();
                step1.setWorkflow(workflow);
                step1.setStepNumber(1);
                step1.setStepType("SEQUENTIAL");
                step1.setAssigneeRole(userRole);
                step1.setRequiredApprovals(1);
                step1.setCreatedAt(LocalDateTime.now());
                step1.setCreatedBy("SYSTEM");

                WorkflowStep step2 = new WorkflowStep();
                step2.setWorkflow(workflow);
                step2.setStepNumber(2);
                step2.setStepType("SEQUENTIAL");
                step2.setAssigneeRole(adminRole);
                step2.setRequiredApprovals(1);
                step2.setCreatedAt(LocalDateTime.now());
                step2.setCreatedBy("SYSTEM");

                workflow.setSteps(List.of(step1, step2));
                workflowRepository.save(workflow);
            }
            log.info("Default sequential workflows seeded successfully matching contract templates.");
        }

        log.info("Database seeding completed successfully.");
    }
}
