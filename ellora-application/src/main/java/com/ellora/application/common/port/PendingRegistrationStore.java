package com.ellora.application.common.port;

import com.ellora.application.feature.auth.model.PendingRegistration;
import java.time.Duration;
import java.util.Optional;

/**
 * Port interface for storing and retrieving unverified registrations in Redis.
 */
public interface PendingRegistrationStore {

    void save(PendingRegistration pendingRegistration, Duration ttl);

    Optional<PendingRegistration> findByEmail(String email);

    void deleteByEmail(String email);
}
