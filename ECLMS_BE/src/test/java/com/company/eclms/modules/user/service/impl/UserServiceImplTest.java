package com.company.eclms.modules.user.service.impl;

import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.modules.department.repository.DepartmentRepository;
import com.company.eclms.modules.role.repository.RoleRepository;
import com.company.eclms.modules.user.dto.UserDto;
import com.company.eclms.modules.user.dto.UserRegistrationDto;
import com.company.eclms.modules.user.entity.User;
import com.company.eclms.modules.user.mapper.UserMapper;
import com.company.eclms.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        registrationDto = UserRegistrationDto.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("password123")
                .fullName("John Doe")
                .roleNames(Set.of())
                .build();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded_password");
        user.setFullName("John Doe");
        user.setStatus("ACTIVE");

        userDto = UserDto.builder()
                .id(user.getId())
                .username("john_doe")
                .email("john@example.com")
                .fullName("John Doe")
                .status("ACTIVE")
                .build();
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act
        UserDto result = userService.createUser(registrationDto);

        // Assert
        assertNotNull(result);
        assertEquals(registrationDto.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> userService.createUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }
}
