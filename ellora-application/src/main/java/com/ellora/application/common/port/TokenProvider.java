package com.ellora.application.common.port;

import java.time.Instant;
import java.util.UUID;

/**
 * Port interface for issuing, decoding, and validating cryptographic tokens (JWT).
 */
public interface TokenProvider {

    String generateAccessToken(UUID userId, String email, String role);

    String generateRefreshToken(UUID userId, UUID familyId);

    boolean validateToken(String token);

    UUID getUserIdFromToken(String token);

    String getRoleFromToken(String token);

    Instant getExpirationFromToken(String token);

    String hashToken(String token);
}
