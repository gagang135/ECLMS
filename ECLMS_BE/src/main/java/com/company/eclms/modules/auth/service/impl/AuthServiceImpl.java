package com.company.eclms.modules.auth.service.impl;

// import com.company.eclms.common.exception.ConflictException;
import com.company.eclms.common.exception.NotFoundException;
import com.company.eclms.common.exception.UnauthorizedException;
import com.company.eclms.common.security.CustomUserDetails;
import com.company.eclms.common.security.CustomUserDetailsService;
import com.company.eclms.common.security.JwtTokenProvider;
import com.company.eclms.modules.auth.dto.LoginRequest;
import com.company.eclms.modules.auth.dto.LoginResponse;
import com.company.eclms.modules.auth.dto.RefreshTokenRequest;
import com.company.eclms.modules.auth.service.AuthService;
import com.company.eclms.modules.user.entity.User;
import com.company.eclms.modules.user.mapper.UserMapper;
import com.company.eclms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    // Fallbacks for local cache if Redis is down
    private final Map<String, String> localOtpCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> localOtpExpiryCache = new ConcurrentHashMap<>();
    private final Map<String, String> localRefreshTokenCache = new ConcurrentHashMap<>();

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (user.isLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
                user.setLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            } else {
                throw new UnauthorizedException("Account is locked. Try again later.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(userDetails);
            String refreshToken = tokenProvider.generateRefreshToken(userDetails);

            // Store refresh token in Redis
            storeRefreshToken(user.getUsername(), refreshToken);

            // Reset failed login attempts
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Log Login History
            logLoginHistory(user.getUsername(), "SUCCESS", ipAddress, userAgent);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userMapper.toDto(user))
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            logLoginHistory(user.getUsername(), "FAILED", ipAddress, userAgent);
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest refreshRequest) {
        String token = refreshRequest.getRefreshToken();
        if (tokenProvider.isTokenExpired(token)) {
            throw new UnauthorizedException("Refresh token is expired");
        }

        String username = tokenProvider.extractUsername(token);
        String savedToken = getStoredRefreshToken(username);

        if (savedToken == null || !savedToken.equals(token)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Rotate token
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

        storeRefreshToken(username, newRefreshToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String jwt = accessToken.substring(7);
            String username = tokenProvider.extractUsername(jwt);

            // Revoke refresh token
            revokeRefreshToken(username);

            // Blacklist access token
            try {
                redisTemplate.opsForValue().set("blacklist:" + jwt, "true", Duration.ofMinutes(15));
            } catch (Exception e) {
                log.warn("Failed to blacklist token in Redis, falling back to log warning");
            }
        }
    }

    @Override
    @Transactional
    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Generate 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        log.info("SECURITY LOG - Generated OTP for user {}: {}", user.getUsername(), otp);

        // Store OTP with 5 mins expiry
        try {
            redisTemplate.opsForValue().set("otp:" + email, otp, Duration.ofMinutes(5));
        } catch (Exception e) {
            localOtpCache.put(email, otp);
            localOtpExpiryCache.put(email, LocalDateTime.now().plusMinutes(5));
        }

        // Email abstraction would trigger here...
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String savedOtp = null;
        try {
            savedOtp = redisTemplate.opsForValue().get("otp:" + email);
        } catch (Exception e) {
            LocalDateTime expiry = localOtpExpiryCache.get(email);
            if (expiry != null && expiry.isAfter(LocalDateTime.now())) {
                savedOtp = localOtpCache.get(email);
            }
        }

        return otp.equals(savedOtp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (!verifyOtp(email, otp)) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate OTP
        try {
            redisTemplate.delete("otp:" + email);
        } catch (Exception e) {
            localOtpCache.remove(email);
            localOtpExpiryCache.remove(email);
        }
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("SECURITY LOG - Account locked for user: {}", user.getUsername());
        }

        userRepository.save(user);
    }

    private void storeRefreshToken(String username, String token) {
        try {
            redisTemplate.opsForValue().set("refresh:" + username, token, Duration.ofDays(7));
        } catch (Exception e) {
            localRefreshTokenCache.put(username, token);
        }
    }

    private String getStoredRefreshToken(String username) {
        try {
            return redisTemplate.opsForValue().get("refresh:" + username);
        } catch (Exception e) {
            return localRefreshTokenCache.get(username);
        }
    }

    private void revokeRefreshToken(String username) {
        try {
            redisTemplate.delete("refresh:" + username);
        } catch (Exception e) {
            localRefreshTokenCache.remove(username);
        }
    }

    private void logLoginHistory(String username, String status, String ip, String userAgent) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO login_histories VALUES (?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID().toString(), username, status, ip, userAgent, LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to write login history for user {}: {}", username, e.getMessage());
        }
    }
}
