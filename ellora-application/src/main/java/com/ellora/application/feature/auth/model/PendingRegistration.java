package com.ellora.application.feature.auth.model;

import java.time.Instant;

/**
 * Record representing temporary pending registration state held in Redis
 * until email OTP verification completes successfully.
 */
public record PendingRegistration(
        String email,
        String passwordHash,
        String fullName,
        String phone,
        String otpHash,
        int attempts,
        Instant createdAt
) {
    public PendingRegistration withIncrementedAttempts() {
        return new PendingRegistration(email, passwordHash, fullName, phone, otpHash, attempts + 1, createdAt);
    }

    public PendingRegistration withNewOtp(String newOtpHash) {
        return new PendingRegistration(email, passwordHash, fullName, phone, newOtpHash, 0, Instant.now());
    }
}
