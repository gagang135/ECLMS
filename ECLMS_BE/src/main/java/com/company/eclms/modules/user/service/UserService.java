package com.company.eclms.modules.user.service;

import com.company.eclms.modules.user.dto.UserDto;
import com.company.eclms.modules.user.dto.UserRegistrationDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    UserDto createUser(UserRegistrationDto registrationDto);
    UserDto getUserById(UUID id);
    UserDto getUserByUsername(String username);
    List<UserDto> getAllUsers();
    UserDto updateUser(UUID id, UserDto userDto);
    void deleteUser(UUID id);
    void lockUser(UUID id);
    void unlockUser(UUID id);
    UserDto assignRolesToUser(UUID userId, Set<UUID> roleIds);
    UserDto assignDepartmentToUser(UUID userId, UUID departmentId);
}
