package com.company.eclms.modules.user.controller;

import com.company.eclms.BaseControllerIntegrationTest;
import com.company.eclms.modules.user.dto.UserDto;
import com.company.eclms.modules.user.entity.User;
import com.company.eclms.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setStatus("ACTIVE");
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(authorities = {"USER_READ", "USER_UPDATE", "USER_DELETE", "USER_LOCK"})
    void userCrud_Success() throws Exception {
        UUID userId = testUser.getId();

        // 1. Get User by ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 2. Get User by Username
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("testuser@example.com"));

        // 3. Update User
        UserDto updateDto = UserDto.builder()
                .username("testuser_updated")
                .email("testuser_updated@example.com")
                .fullName("Updated Test User")
                .status("ACTIVE")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Test User"));

        // 4. Lock User
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/" + userId + "/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Unlock User
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/" + userId + "/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Delete User
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify Deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }
}
