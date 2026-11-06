package com.ellora.application.common.port;

import java.time.Instant;

/**
 * Port interface for managing revoked/blacklisted access tokens.
 */
public interface TokenBlacklist {

    void revokeToken(String tokenHash, Instant expiresAt);

    boolean isRevoked(String tokenHash);
}
