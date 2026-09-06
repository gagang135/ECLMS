package com.company.eclms.modules.workflow.mapper;

import com.company.eclms.modules.workflow.dto.WorkflowStepDto;
import com.company.eclms.modules.workflow.entity.WorkflowStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowStepMapper {

    @Mapping(source = "assigneeRole.id", target = "assigneeRoleId")
    @Mapping(source = "assigneeRole.name", target = "assigneeRoleName")
    @Mapping(source = "assigneeUser.id", target = "assigneeUserId")
    @Mapping(source = "assigneeUser.fullName", target = "assigneeUserName")
    WorkflowStepDto toDto(WorkflowStep step);

    @Mapping(source = "assigneeRoleId", target = "assigneeRole.id")
    @Mapping(source = "assigneeUserId", target = "assigneeUser.id")
    WorkflowStep toEntity(WorkflowStepDto stepDto);
}
