package com.company.eclms.modules.user.dto;

import com.company.eclms.modules.role.dto.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private UUID departmentId;
    private String departmentName;
    private String status;
    private boolean locked;
    private Set<RoleDto> roles;
}
