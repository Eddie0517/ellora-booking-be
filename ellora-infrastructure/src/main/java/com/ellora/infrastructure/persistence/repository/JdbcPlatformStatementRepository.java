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
    public Optional<StatementPaymentRecord> findPendingPayment(Long salonId, String billingMonth) {
        String sql = "SELECT id, salon_id, billing_month, amount, payos_order_code, payos_payment_link_id, "
                + "payos_checkout_url, payos_qr_code, status, payment_reference, payment_reported_at, created_at "
                + "FROM platform_statement_payments WHERE salon_id = ? AND billing_month = ? AND status = 'PENDING' "
                + "ORDER BY id DESC LIMIT 1";
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            Timestamp repTs = rs.getTimestamp("payment_reported_at");
            Timestamp crTs = rs.getTimestamp("created_at");
            return Optional.of(new StatementPaymentRecord(
                    rs.getLong("id"),
                    rs.getLong("salon_id"),
                    rs.getString("billing_month"),
                    rs.getBigDecimal("amount"),
                    rs.getLong("payos_order_code"),
                    rs.getString("payos_payment_link_id"),
                    rs.getString("payos_checkout_url"),
                    rs.getString("payos_qr_code"),
                    rs.getString("status"),
                    rs.getString("payment_reference"),
                    repTs != null ? repTs.toInstant() : null,
                    crTs != null ? crTs.toInstant() : null
            ));
        }, salonId, billingMonth);
    }

    @Override
    public void savePayment(Long salonId, String billingMonth, BigDecimal amount, Long orderCode,
                            String paymentLinkId, String checkoutUrl, String qrCode) {
        String sql = "INSERT INTO platform_statement_payments (salon_id, billing_month, amount, payos_order_code, "
                + "payos_payment_link_id, payos_checkout_url, payos_qr_code, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbc.update(sql, salonId, billingMonth, amount, orderCode, paymentLinkId, checkoutUrl, qrCode);
    }

    @Override
    public boolean markPaymentPaid(Long orderCode, String paymentReference, Instant reportedAt) {
        String sql = "UPDATE platform_statement_payments SET status = 'PAID', payment_reference = ?, "
                + "payment_reported_at = ?, updated_at = CURRENT_TIMESTAMP WHERE payos_order_code = ? AND status = 'PENDING'";
        int updated = jdbc.update(sql, paymentReference, Timestamp.from(reportedAt != null ? reportedAt : Instant.now()), orderCode);
        return updated > 0;
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
