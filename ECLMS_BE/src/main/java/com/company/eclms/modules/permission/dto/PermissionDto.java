package com.company.eclms.modules.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {

    private UUID id;

    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Permission group is required")
    @Size(max = 100, message = "Permission group cannot exceed 100 characters")
    private String permissionGroup;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
