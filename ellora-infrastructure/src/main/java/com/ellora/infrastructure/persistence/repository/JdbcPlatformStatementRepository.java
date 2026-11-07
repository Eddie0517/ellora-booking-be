package com.ellora.infrastructure.persistence.repository;

import com.ellora.domain.repository.PlatformStatementRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPlatformStatementRepository implements PlatformStatementRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<StatementRecord> findBySalonIdAndMonth(Long salonId, LocalDate month) {
        String sql = "SELECT id, salon_id, billing_month, paid_amount, paid_at, confirmed_by, reminded_at "
                + "FROM platform_statements WHERE salon_id = ? AND billing_month = ?";
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            Timestamp paidAtTs = rs.getTimestamp("paid_at");
            Timestamp remindedAtTs = rs.getTimestamp("reminded_at");
            String confirmedByStr = rs.getString("confirmed_by");
            return Optional.of(new StatementRecord(
                    rs.getLong("id"),
                    rs.getLong("salon_id"),
                    rs.getDate("billing_month").toLocalDate(),
                    rs.getBigDecimal("paid_amount"),
                    paidAtTs != null ? paidAtTs.toInstant() : null,
                    confirmedByStr != null ? UUID.fromString(confirmedByStr) : null,
                    remindedAtTs != null ? remindedAtTs.toInstant() : null
            ));
        }, salonId, java.sql.Date.valueOf(month));
    }

    @Override
    public void recordPayment(Long salonId, LocalDate month, BigDecimal additionalPaid, UUID confirmedBy) {
        String sql = "INSERT INTO platform_statements (salon_id, billing_month, paid_amount, paid_at, confirmed_by) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?) "
                + "ON CONFLICT (salon_id, billing_month) DO UPDATE SET "
                + "paid_amount = platform_statements.paid_amount + EXCLUDED.paid_amount, "
                + "paid_at = CURRENT_TIMESTAMP, "
                + "confirmed_by = EXCLUDED.confirmed_by";
        jdbc.update(sql, salonId, java.sql.Date.valueOf(month), additionalPaid, confirmedBy);
    }

    @Override
    public void recordReminder(Long salonId, LocalDate month) {
        String sql = "INSERT INTO platform_statements (salon_id, billing_month, paid_amount, reminded_at) "
                + "VALUES (?, ?, 0, CURRENT_TIMESTAMP) "
                + "ON CONFLICT (salon_id, billing_month) DO UPDATE SET reminded_at = CURRENT_TIMESTAMP";
        jdbc.update(sql, salonId, java.sql.Date.valueOf(month));
    }
}
