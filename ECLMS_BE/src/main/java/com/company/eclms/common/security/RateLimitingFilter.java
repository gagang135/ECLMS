package com.company.eclms.common.security;

import com.company.eclms.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    // Fallback in-memory rate limiting mechanism
    private final ConcurrentHashMap<String, RequestBucket> localLimiters = new ConcurrentHashMap<>();

    private static class RequestBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String key = resolveRateLimitKey(request);
        boolean allowed = isAllowed(key);

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}", key);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<Void> apiResponse = ApiResponse.error(
                    "Too many requests. Please try again later.",
                    "Rate limit exceeded"
            );

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writeValue(response.getOutputStream(), apiResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveRateLimitKey(HttpServletRequest request) {
        String username = "ANONYMOUS";
        if (request.getUserPrincipal() != null) {
            username = request.getUserPrincipal().getName();
        }
        String ip = request.getRemoteAddr();
        return "rate_limit:" + username + ":" + ip;
    }

    private boolean isAllowed(String key) {
        try {
            // Using Redis with a simple fixed-window strategy
            String currentVal = redisTemplate.opsForValue().get(key);
            if (currentVal == null) {
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
                return true;
            }

            int count = Integer.parseInt(currentVal);
            if (count < requestsPerMinute) {
                redisTemplate.opsForValue().increment(key);
                return true;
            }
            return false;

        } catch (Exception e) {
            // Fallback to local in-memory limits if Redis is down
            long now = System.currentTimeMillis();
            RequestBucket bucket = localLimiters.computeIfAbsent(key, k -> new RequestBucket());

            if (now - bucket.timestamp.get() > 60000) {
                bucket.timestamp.set(now);
                bucket.count.set(1);
                return true;
            }

            return bucket.count.incrementAndGet() <= requestsPerMinute;
        }
    }
}
