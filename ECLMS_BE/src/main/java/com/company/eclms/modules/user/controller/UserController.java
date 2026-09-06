package com.company.eclms.modules.user.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.user.dto.UserDto;
import com.company.eclms.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<UserDto> getUserById(@PathVariable UUID id) {
        UserDto userDto = userService.getUserById(id);
        return ApiResponse.success(userDto);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<UserDto> getUserByUsername(@PathVariable String username) {
        UserDto userDto = userService.getUserByUsername(username);
        return ApiResponse.success(userDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
        UserDto updated = userService.updateUser(id, userDto);
        return ApiResponse.success(updated, "User updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ApiResponse.success(null, "User deleted successfully");
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('USER_LOCK')")
    public ApiResponse<Void> lockUser(@PathVariable UUID id) {
        userService.lockUser(id);
        return ApiResponse.success(null, "User locked successfully");
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_LOCK')")
    public ApiResponse<Void> unlockUser(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ApiResponse.success(null, "User unlocked successfully");
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_ASSIGN_ROLE')")
    public ApiResponse<UserDto> assignRoles(@PathVariable UUID id, @RequestBody Set<UUID> roleIds) {
        UserDto updated = userService.assignRolesToUser(id, roleIds);
        return ApiResponse.success(updated, "Roles assigned successfully");
    }

    @PostMapping("/{id}/department")
    @PreAuthorize("hasAuthority('USER_ASSIGN_DEPARTMENT')")
    public ApiResponse<UserDto> assignDepartment(@PathVariable UUID id, @RequestParam UUID departmentId) {
        UserDto updated = userService.assignDepartmentToUser(id, departmentId);
        return ApiResponse.success(updated, "Department assigned successfully");
    }
}
