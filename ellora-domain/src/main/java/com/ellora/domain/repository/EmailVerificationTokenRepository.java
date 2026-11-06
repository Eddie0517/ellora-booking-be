package com.ellora.domain.repository;

import com.ellora.domain.model.EmailVerificationToken;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for managing email verification tokens.
 */
public interface EmailVerificationTokenRepository {

    EmailVerificationToken save(EmailVerificationToken token);

    Optional<EmailVerificationToken> findLatestByUserId(UUID userId);

    void invalidateAllActiveByUserId(UUID userId);
}
