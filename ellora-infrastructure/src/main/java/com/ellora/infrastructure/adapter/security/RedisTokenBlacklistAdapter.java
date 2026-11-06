package com.ellora.infrastructure.adapter.security;

import com.ellora.application.common.port.TokenBlacklist;
import com.ellora.infrastructure.persistence.entity.RevokedAccessTokenEntity;
import com.ellora.infrastructure.persistence.repository.SpringDataRevokedAccessTokenRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistAdapter implements TokenBlacklist {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklistAdapter.class);
    private static final String KEY_PREFIX = "ellora:revoked:";

    private final StringRedisTemplate redisTemplate;
    private final SpringDataRevokedAccessTokenRepository dbRepository;

    @Override
    public void revokeToken(String tokenHash, Instant expiresAt) {
        if (tokenHash == null || expiresAt == null) {
            return;
        }

        // 1. Save to PostgreSQL
        try {
            dbRepository.save(new RevokedAccessTokenEntity(tokenHash, expiresAt));
        } catch (Exception e) {
            log.error("Failed to persist revoked token to PostgreSQL: {}", e.getMessage());
        }

        // 2. Cache in Redis
        try {
            long secondsUntilExp = Duration.between(Instant.now(), expiresAt).getSeconds();
            if (secondsUntilExp > 0) {
                redisTemplate.opsForValue().set(KEY_PREFIX + tokenHash, "1", Duration.ofSeconds(secondsUntilExp));
            }
        } catch (Exception e) {
            log.warn("Failed to cache revoked token in Redis (fail-open resilience): {}", e.getMessage());
        }
    }

    @Override
    public boolean isRevoked(String tokenHash) {
        if (tokenHash == null) {
            return false;
        }

        // 1. Check Redis first
        try {
            Boolean exists = redisTemplate.hasKey(KEY_PREFIX + tokenHash);
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Redis unreachable when checking token blacklist; falling back to DB: {}", e.getMessage());
        }

        // 2. Fallback to DB
        try {
            return dbRepository.existsById(tokenHash);
        } catch (Exception e) {
            log.error("Database error while checking token revocation: {}", e.getMessage());
            return false;
        }
    }
}
