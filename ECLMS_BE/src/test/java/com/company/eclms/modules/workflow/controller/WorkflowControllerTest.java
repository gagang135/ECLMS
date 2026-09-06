package com.company.eclms.modules.workflow.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.common.security.CustomUserDetails;
import com.company.eclms.modules.contract.entity.Contract;
import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.department.entity.Department;
import com.company.eclms.modules.department.repository.DepartmentRepository;
import com.company.eclms.modules.user.entity.User;
import com.company.eclms.modules.user.repository.UserRepository;
import com.company.eclms.modules.vendor.entity.Vendor;
import com.company.eclms.modules.vendor.repository.VendorRepository;
import com.company.eclms.modules.workflow.dto.WorkflowApprovalRequest;
import com.company.eclms.modules.workflow.dto.WorkflowDto;
import com.company.eclms.modules.workflow.dto.WorkflowStepDto;
import com.company.eclms.modules.workflow.repository.WorkflowApprovalRepository;
import com.company.eclms.modules.workflow.repository.WorkflowInstanceRepository;
import com.company.eclms.modules.workflow.repository.WorkflowRepository;
import com.company.eclms.modules.workflow.repository.WorkflowStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkflowControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowStepRepository workflowStepRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowApprovalRepository workflowApprovalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ContractRepository contractRepository;

    private User testUser;
    private Contract contract;

    @BeforeEach
    void setUp() {
        workflowApprovalRepository.deleteAll();
        workflowInstanceRepository.deleteAll();
        workflowStepRepository.deleteAll();
        workflowRepository.deleteAll();
        contractRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        vendorRepository.deleteAll();

        // 1. Create a user
        testUser = new User();
        testUser.setUsername("workflow_user");
        testUser.setEmail("wf@test.com");
        testUser.setPassword("password");
        testUser.setFullName("Workflow User");
        testUser.setStatus("ACTIVE");
        testUser = userRepository.save(testUser);

        // 2. Set Security Context manually
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .password(testUser.getPassword())
                .enabled(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("WORKFLOW_CREATE"),
                        new SimpleGrantedAuthority("WORKFLOW_READ"),
                        new SimpleGrantedAuthority("WORKFLOW_START"),
                        new SimpleGrantedAuthority("WORKFLOW_APPROVE")
                ))
                .build();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Create Department, Vendor, Contract
        Department department = new Department();
        department.setName("Finance");
        department = departmentRepository.save(department);

        Vendor vendor = new Vendor();
        vendor.setName("Finance Vendor");
        vendor.setEmail("finance@vendor.com");
        vendor = vendorRepository.save(vendor);

        contract = new Contract();
        contract.setName("Finance Agreement");
        contract.setDepartment(department);
        contract.setVendor(vendor);
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusYears(1));
        contract.setStatus("DRAFT");
        contract.setContent("Finance contract content details");
        contract = contractRepository.save(contract);
    }

    @Test
    void workflowOperations_Success() throws Exception {
        // 1. Create Workflow Template
        WorkflowStepDto stepDto = WorkflowStepDto.builder()
                .stepNumber(1)
                .stepType("SEQUENTIAL")
                .assigneeUserId(testUser.getId())
                .requiredApprovals(1)
                .build();

        WorkflowDto workflowDto = WorkflowDto.builder()
                .name("NDA Approval Workflow")
                .description("Simple approval workflow for NDAs")
                .steps(List.of(stepDto))
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workflowDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NDA Approval Workflow"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UUID workflowId = UUID.fromString(objectMapper.readTree(responseContent).path("data").path("id").asText());

        // 2. Read Workflow Template
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/workflows/" + workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NDA Approval Workflow"));

        // 3. Start Workflow Instance
        MvcResult startResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workflows/start")
                        .param("workflowId", workflowId.toString())
                        .param("contractId", contract.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andReturn();

        String startResponse = startResult.getResponse().getContentAsString();
        UUID instanceId = UUID.fromString(objectMapper.readTree(startResponse).path("data").path("id").asText());

        // 4. Approve Step
        WorkflowApprovalRequest approvalRequest = new WorkflowApprovalRequest("APPROVED", "Looks good");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workflows/instances/" + instanceId + "/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Get Approval History
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/workflows/instances/" + instanceId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));
    }
}
