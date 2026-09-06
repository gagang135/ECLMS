package com.company.eclms.modules.workflow.mapper;

import com.company.eclms.modules.workflow.dto.WorkflowApprovalDto;
import com.company.eclms.modules.workflow.entity.WorkflowApproval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowApprovalMapper {

    @Mapping(source = "workflowInstance.id", target = "workflowInstanceId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    WorkflowApprovalDto toDto(WorkflowApproval approval);

    @Mapping(source = "workflowInstanceId", target = "workflowInstance.id")
    @Mapping(source = "userId", target = "user.id")
    WorkflowApproval toEntity(WorkflowApprovalDto approvalDto);
}
