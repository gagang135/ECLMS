package com.company.eclms.modules.document.mapper;

import com.company.eclms.modules.document.dto.DocumentDto;
import com.company.eclms.modules.document.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(target = "previewUrl", ignore = true)
    DocumentDto toDto(Document document);

    @Mapping(source = "contractId", target = "contract.id")
    Document toEntity(DocumentDto documentDto);
}
