package com.ellora.application.common.port;

import java.time.Duration;
import java.util.Optional;

public interface PasswordResetStore {

    void save(String email, String otpCode, Duration ttl);

    Optional<String> findOtpByEmail(String email);

    void deleteByEmail(String email);
}
