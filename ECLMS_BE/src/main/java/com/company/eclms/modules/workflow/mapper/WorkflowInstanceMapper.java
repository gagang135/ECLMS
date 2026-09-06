package com.company.eclms.modules.workflow.mapper;

import com.company.eclms.modules.workflow.dto.WorkflowInstanceDto;
import com.company.eclms.modules.workflow.entity.WorkflowInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowInstanceMapper {

    @Mapping(source = "workflow.id", target = "workflowId")
    @Mapping(source = "workflow.name", target = "workflowName")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(source = "contract.name", target = "contractName")
    WorkflowInstanceDto toDto(WorkflowInstance instance);

    @Mapping(source = "workflowId", target = "workflow.id")
    @Mapping(source = "contractId", target = "contract.id")
    WorkflowInstance toEntity(WorkflowInstanceDto instanceDto);
}
