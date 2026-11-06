package com.ellora.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an email verification OTP token.
 */
public class EmailVerificationToken {

    public static final int MAX_ATTEMPTS = 5;

    private UUID id;
    private UUID userId;
    private String codeHash;
    private Instant expiresAt;
    private Instant consumedAt;
    private int attempts;
    private Instant createdAt;

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(UUID id, UUID userId, String codeHash, Instant expiresAt,
                                  Instant consumedAt, int attempts, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.attempts = attempts;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static EmailVerificationToken create(UUID userId, String codeHash, long ttlHours) {
        Instant now = Instant.now();
        return new EmailVerificationToken(
                UUID.randomUUID(),
                userId,
                codeHash,
                now.plusSeconds(ttlHours * 3600),
                null,
                0,
                now
        );
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean hasExceededAttempts() {
        return attempts >= MAX_ATTEMPTS;
    }

    public boolean isValid() {
        return !isConsumed() && !isExpired() && !hasExceededAttempts();
    }

    public void recordFailedAttempt() {
        this.attempts++;
    }

    public void markConsumed() {
        this.consumedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
