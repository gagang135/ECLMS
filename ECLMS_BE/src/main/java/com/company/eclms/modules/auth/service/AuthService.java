package com.company.eclms.modules.auth.service;

import com.company.eclms.modules.auth.dto.LoginRequest;
import com.company.eclms.modules.auth.dto.LoginResponse;
import com.company.eclms.modules.auth.dto.RefreshTokenRequest;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest, String ipAddress, String userAgent);
    LoginResponse refresh(RefreshTokenRequest refreshRequest);
    void logout(String accessToken);
    void sendOtp(String email);
    boolean verifyOtp(String email, String otp);
    void resetPassword(String email, String otp, String newPassword);
}
