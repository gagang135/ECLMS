package com.company.eclms.modules.workflow.mapper;

import com.company.eclms.modules.workflow.dto.WorkflowDto;
import com.company.eclms.modules.workflow.entity.Workflow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {WorkflowStepMapper.class})
public interface WorkflowMapper {
    WorkflowDto toDto(Workflow workflow);
    Workflow toEntity(WorkflowDto workflowDto);
}
