package com.ellora.infrastructure.persistence.repository;

import com.ellora.domain.repository.PlatformBillingSettingsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPlatformBillingSettingsRepository implements PlatformBillingSettingsRepository {
    private final JdbcTemplate jdbc;

    @Override
    public Optional<String> findQrUrl() {
        return jdbc.query("SELECT qr_url FROM platform_billing_settings WHERE id = 1",
                rs -> rs.next() ? Optional.ofNullable(rs.getString("qr_url")) : Optional.empty());
    }

    @Override
    public void updateQrUrl(String qrUrl) {
        jdbc.update("INSERT INTO platform_billing_settings (id, qr_url) VALUES (1, ?) "
                + "ON CONFLICT (id) DO UPDATE SET qr_url = EXCLUDED.qr_url", qrUrl);
    }
}
