package com.ellora.domain.repository;

import java.util.Optional;

public interface PlatformBillingSettingsRepository {
    Optional<String> findQrUrl();
    void updateQrUrl(String qrUrl);
}
