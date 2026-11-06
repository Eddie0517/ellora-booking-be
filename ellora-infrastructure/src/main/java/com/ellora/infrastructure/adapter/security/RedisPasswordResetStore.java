package com.ellora.infrastructure.adapter.security;

import com.ellora.application.common.port.PasswordResetStore;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPasswordResetStore implements PasswordResetStore {

    private static final Logger log = LoggerFactory.getLogger(RedisPasswordResetStore.class);
    private static final String KEY_PREFIX = "ellora:password_reset:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String email, String otpCode, Duration ttl) {
        if (email == null || otpCode == null) {
            return;
        }
        String key = KEY_PREFIX + email.toLowerCase().trim();
        try {
            redisTemplate.opsForValue().set(key, otpCode.trim(), ttl);
            log.info("Saved password reset OTP in Redis for email: {} with TTL: {}s", email, ttl.getSeconds());
        } catch (Exception e) {
            log.error("Failed to save password reset OTP to Redis for {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Lỗi lưu trữ mã xác nhận đặt lại mật khẩu", e);
        }
    }

    @Override
    public Optional<String> findOtpByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + email.toLowerCase().trim();
        try {
            String otp = redisTemplate.opsForValue().get(key);
            if (otp == null || otp.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(otp.trim());
        } catch (Exception e) {
            log.error("Failed to read password reset OTP from Redis for {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteByEmail(String email) {
        if (email == null) {
            return;
        }
        String key = KEY_PREFIX + email.toLowerCase().trim();
        try {
            redisTemplate.delete(key);
            log.info("Deleted password reset OTP from Redis for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to delete password reset OTP from Redis for {}: {}", email, e.getMessage(), e);
        }
    }
}
