package com.ellora.infrastructure.adapter.security;

import com.ellora.application.common.port.PendingRegistrationStore;
import com.ellora.application.feature.auth.model.PendingRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPendingRegistrationStore implements PendingRegistrationStore {

    private static final Logger log = LoggerFactory.getLogger(RedisPendingRegistrationStore.class);
    private static final String KEY_PREFIX = "ellora:pending_registration:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(PendingRegistration pendingRegistration, Duration ttl) {
        if (pendingRegistration == null || pendingRegistration.email() == null) {
            return;
        }
        String key = KEY_PREFIX + pendingRegistration.email().toLowerCase().trim();
        try {
            String json = objectMapper.writeValueAsString(pendingRegistration);
            redisTemplate.opsForValue().set(key, json, ttl);
            log.info("Saved pending registration in Redis for email: {} with TTL: {}s", pendingRegistration.email(), ttl.getSeconds());
        } catch (Exception e) {
            log.error("Failed to save pending registration to Redis for {}: {}", pendingRegistration.email(), e.getMessage(), e);
            throw new RuntimeException("Lỗi lưu trữ thông tin đăng ký tạm thời", e);
        }
    }

    @Override
    public Optional<PendingRegistration> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + email.toLowerCase().trim();
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, PendingRegistration.class));
        } catch (Exception e) {
            log.error("Failed to read pending registration from Redis for {}: {}", email, e.getMessage(), e);
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
            log.info("Deleted pending registration from Redis for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to delete pending registration from Redis for {}: {}", email, e.getMessage(), e);
        }
    }
}
