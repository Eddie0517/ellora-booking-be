package com.ellora.infrastructure.persistence.repository;

import com.ellora.domain.repository.PlatformFeePolicyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPlatformFeePolicyRepository implements PlatformFeePolicyRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<BigDecimal> findRateForMonth(LocalDate month) {
        String sql = "SELECT percent FROM platform_fee_policies WHERE effective_month <= ? ORDER BY effective_month DESC LIMIT 1";
        return jdbc.query(sql, rs -> rs.next() ? Optional.of(rs.getBigDecimal("percent")) : Optional.empty(), java.sql.Date.valueOf(month));
    }

    @Override
    public void saveRate(LocalDate effectiveMonth, BigDecimal percent) {
        String sql = "INSERT INTO platform_fee_policies (effective_month, percent) VALUES (?, ?) "
                + "ON CONFLICT (effective_month) DO UPDATE SET percent = EXCLUDED.percent";
        jdbc.update(sql, java.sql.Date.valueOf(effectiveMonth), percent);
    }
}
