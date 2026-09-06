package com.company.eclms.modules.auth.controller;

import com.company.eclms.common.response.ApiResponse;
import com.company.eclms.modules.auth.dto.LoginRequest;
import com.company.eclms.modules.auth.dto.LoginResponse;
import com.company.eclms.modules.auth.dto.RefreshTokenRequest;
import com.company.eclms.modules.auth.service.AuthService;
import com.company.eclms.modules.user.dto.UserDto;
import com.company.eclms.modules.user.dto.UserRegistrationDto;
import com.company.eclms.modules.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserDto userDto = userService.createUser(registrationDto);
        return ApiResponse.success(userDto, "User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        LoginResponse response = authService.login(loginRequest, ipAddress, userAgent);
        return ApiResponse.success(response, "Login successful");
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        LoginResponse response = authService.refresh(refreshRequest);
        return ApiResponse.success(response, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ApiResponse.success(null, "Logout successful");
    }

    @PostMapping("/otp/send")
    public ApiResponse<Void> sendOtp(@RequestParam String email) {
        authService.sendOtp(email);
        return ApiResponse.success(null, "OTP sent to " + email);
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        authService.resetPassword(email, otp, newPassword);
        return ApiResponse.success(null, "Password reset successful");
    }
}
