package com.ellora.domain.repository;

import com.ellora.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for Refresh Token persistence.
 */
public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    void revokeAllByFamilyId(UUID familyId);

    void revokeAllByUserId(UUID userId);
}
