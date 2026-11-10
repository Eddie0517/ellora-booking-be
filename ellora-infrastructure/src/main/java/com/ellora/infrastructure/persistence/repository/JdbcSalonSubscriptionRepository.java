package com.ellora.infrastructure.persistence.repository;

import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.SalonSubscriptionPlan;
import com.ellora.domain.model.SalonSubscriptionRequest;
import com.ellora.domain.model.SalonSubscriptionSnapshot;
import com.ellora.domain.repository.SalonSubscriptionRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcSalonSubscriptionRepository implements SalonSubscriptionRepository {
    private static final String REQUEST_SELECT = "SELECT r.*, s.name AS salon_name FROM salon_subscription_requests r "
            + "JOIN salons s ON s.id = r.salon_id ";
    private final JdbcTemplate jdbc;

    private List<String> parseFeatures(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return java.util.Arrays.stream(raw.split("[;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public List<SalonSubscriptionPlan> findPlans() {
        return jdbc.query("SELECT tier, monthly_price, enabled, description, features FROM salon_subscription_plans ORDER BY tier", (rs, row) ->
                new SalonSubscriptionPlan(rs.getString("tier"), rs.getBigDecimal("monthly_price"), rs.getBoolean("enabled"),
                        rs.getString("description"), parseFeatures(rs.getString("features"))));
    }

    @Override
    public Optional<SalonSubscriptionPlan> findPlan(String tier) {
        return jdbc.query("SELECT tier, monthly_price, enabled, description, features FROM salon_subscription_plans WHERE tier = ?", (rs, row) ->
                new SalonSubscriptionPlan(rs.getString("tier"), rs.getBigDecimal("monthly_price"), rs.getBoolean("enabled"),
                        rs.getString("description"), parseFeatures(rs.getString("features"))), tier)
                .stream().findFirst();
    }

    @Override
    public SalonSubscriptionPlan savePlan(String tier, BigDecimal monthlyPrice, boolean enabled, UUID adminId) {
        return savePlan(tier, monthlyPrice, enabled, null, List.of(), adminId);
    }

    @Override
    public SalonSubscriptionPlan savePlan(String tier, BigDecimal monthlyPrice, boolean enabled, String description, List<String> features, UUID adminId) {
        String featuresRaw = (features == null || features.isEmpty()) ? null : String.join(";", features);
        jdbc.update("INSERT INTO salon_subscription_plans(tier, monthly_price, enabled, description, features, updated_at, updated_by) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?) ON CONFLICT (tier) DO UPDATE SET "
                        + "monthly_price=EXCLUDED.monthly_price, enabled=EXCLUDED.enabled, "
                        + "description=EXCLUDED.description, features=EXCLUDED.features, "
                        + "updated_at=CURRENT_TIMESTAMP, updated_by=EXCLUDED.updated_by",
                tier, monthlyPrice, enabled, description, featuresRaw, adminId);
        return findPlan(tier).orElseThrow();
    }

    @Override
    public void cancelRequest(Long requestId) {
        jdbc.update("DELETE FROM salon_subscription_requests WHERE id = ? AND status = 'PENDING' AND payment_reported_at IS NULL", requestId);
    }

    @Override
    public Optional<Long> findSalonIdByOwner(UUID ownerId) {
        return jdbc.query("SELECT id FROM salons WHERE owner_id = ? ORDER BY id LIMIT 1",
                (rs, row) -> rs.getLong("id"), ownerId).stream().findFirst();
    }

    @Override
    public Optional<SalonSubscriptionSnapshot> findCurrentSubscription(Long salonId) {
        return jdbc.query("SELECT id, subscription_tier, subscription_starts_at, subscription_expires_at FROM salons WHERE id = ?",
                (rs, row) -> new SalonSubscriptionSnapshot(rs.getLong("id"), rs.getString("subscription_tier"),
                        instant(rs, "subscription_starts_at"), instant(rs, "subscription_expires_at")), salonId).stream().findFirst();
    }

    @Override
    public Optional<SalonSubscriptionRequest> findPendingRequest(Long salonId) {
        return jdbc.query(REQUEST_SELECT + "WHERE r.salon_id = ? AND r.status = 'PENDING' ORDER BY r.id DESC LIMIT 1",
                REQUEST_MAPPER, salonId).stream().findFirst();
    }

    @Override
    public Optional<SalonSubscriptionRequest> findRequest(Long requestId) {
        return jdbc.query(REQUEST_SELECT + "WHERE r.id = ?", REQUEST_MAPPER, requestId).stream().findFirst();
    }

    @Override
    public List<SalonSubscriptionRequest> findOwnerRequests(Long salonId, int offset, int limit) {
        return jdbc.query(REQUEST_SELECT + "WHERE r.salon_id = ? ORDER BY r.created_at DESC, r.id DESC OFFSET ? LIMIT ?",
                REQUEST_MAPPER, salonId, offset, limit);
    }

    @Override
    public long countOwnerRequests(Long salonId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM salon_subscription_requests WHERE salon_id = ?", Long.class, salonId);
        return count == null ? 0 : count;
    }

    @Override
    public SalonSubscriptionRequest createRequest(Long salonId, String tier, BigDecimal quotedPrice) {
        Long id = jdbc.queryForObject("INSERT INTO salon_subscription_requests(salon_id, tier, quoted_price, status) "
                + "VALUES (?, ?, ?, 'PENDING') RETURNING id", Long.class, salonId, tier, quotedPrice);
        return findRequest(id).orElseThrow();
    }

    @Override
    public Long nextPayosOrderCode() {
        return jdbc.queryForObject("SELECT nextval('payos_subscription_order_code')", Long.class);
    }

    @Override
    public SalonSubscriptionRequest savePayosPayment(Long salonId, Long requestId, Long orderCode, String paymentLinkId, String checkoutUrl, String qrCode) {
        int updated = jdbc.update("UPDATE salon_subscription_requests SET payos_order_code=?, payos_payment_link_id=?, "
                        + "payos_checkout_url=?, payos_qr_code=? WHERE id=? AND salon_id=? AND status='PENDING' AND payos_order_code IS NULL",
                orderCode, paymentLinkId, checkoutUrl, qrCode, requestId, salonId);
        if (updated == 0) throw requestError(requestId, salonId);
        return findRequest(requestId).orElseThrow();
    }

    @Override
    public void confirmPayosPayment(Long orderCode, String paymentLinkId, BigDecimal amount, String reference) {
        int updated = jdbc.update("UPDATE salon_subscription_requests SET payment_reported_at=CURRENT_TIMESTAMP, "
                        + "payment_reference=?, received_amount=? WHERE payos_order_code=? AND payos_payment_link_id=? "
                        + "AND quoted_price=? AND status='PENDING' AND payment_reported_at IS NULL",
                reference, amount, orderCode, paymentLinkId, amount);
        if (updated == 0) {
            Boolean known = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM salon_subscription_requests WHERE payos_order_code=?)",
                    Boolean.class, orderCode);
            if (!Boolean.TRUE.equals(known)) return;
            Boolean duplicate = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM salon_subscription_requests WHERE payos_order_code=? "
                    + "AND payos_payment_link_id=? AND received_amount=? AND payment_reported_at IS NOT NULL)",
                    Boolean.class, orderCode, paymentLinkId, amount);
            if (!Boolean.TRUE.equals(duplicate)) throw new BusinessRuleException("PAYMENT_MISMATCH", "Giao dịch PayOS không khớp yêu cầu gói");
        }
    }

    @Override
    public void resetPayosPayment(Long requestId, Long orderCode) {
        jdbc.update("UPDATE salon_subscription_requests SET payos_order_code=NULL, payos_payment_link_id=NULL, "
                        + "payos_checkout_url=NULL, payos_qr_code=NULL WHERE id=? AND payos_order_code=? "
                        + "AND status='PENDING' AND payment_reported_at IS NULL", requestId, orderCode);
    }

    @Override
    public List<SalonSubscriptionRequest> findAdminRequests(String status, int offset, int limit) {
        if (status == null) return jdbc.query(REQUEST_SELECT + "ORDER BY r.created_at DESC, r.id DESC OFFSET ? LIMIT ?", REQUEST_MAPPER, offset, limit);
        return jdbc.query(REQUEST_SELECT + "WHERE r.status = ? ORDER BY r.created_at DESC, r.id DESC OFFSET ? LIMIT ?", REQUEST_MAPPER, status, offset, limit);
    }

    @Override
    public long countAdminRequests(String status) {
        String sql = status == null ? "SELECT COUNT(*) FROM salon_subscription_requests" : "SELECT COUNT(*) FROM salon_subscription_requests WHERE status = ?";
        Long count = status == null ? jdbc.queryForObject(sql, Long.class) : jdbc.queryForObject(sql, Long.class, status);
        return count == null ? 0 : count;
    }

    @Override
    public SalonSubscriptionRequest decideRequest(Long requestId, UUID adminId, String status, String note,
            Instant startsAt, Instant expiresAt, BigDecimal receivedAmount) {
        int updated;
        if ("APPROVED".equals(status)) {
            updated = jdbc.update("UPDATE salon_subscription_requests SET status='APPROVED', reviewed_at=CURRENT_TIMESTAMP, "
                            + "review_note=?, reviewed_by=?, starts_at=?, expires_at=?, payment_confirmed_at=CURRENT_TIMESTAMP, "
                            + "payment_confirmed_by=?, received_amount=? WHERE id=? AND status='PENDING' "
                            + "AND payment_reported_at IS NOT NULL AND payos_order_code IS NOT NULL AND received_amount=?",
                    note, adminId, timestamp(startsAt), timestamp(expiresAt), adminId, receivedAmount, requestId, receivedAmount);
            if (updated > 0) {
                jdbc.update("UPDATE salons SET subscription_tier=(SELECT tier FROM salon_subscription_requests WHERE id=?), "
                                + "subscription_starts_at=?, subscription_expires_at=?, updated_at=CURRENT_TIMESTAMP "
                                + "WHERE id=(SELECT salon_id FROM salon_subscription_requests WHERE id=?)",
                        requestId, timestamp(startsAt), timestamp(expiresAt), requestId);
            }
        } else {
            updated = jdbc.update("UPDATE salon_subscription_requests SET status='REJECTED', reviewed_at=CURRENT_TIMESTAMP, "
                            + "review_note=?, reviewed_by=? WHERE id=? AND status='PENDING'",
                    note, adminId, requestId);
        }
        if (updated == 0) throw new BusinessRuleException("SUBSCRIPTION_REQUEST_NOT_PENDING", "Yêu cầu đã được xử lý hoặc chưa báo thanh toán");
        return findRequest(requestId).orElseThrow();
    }

    private BusinessRuleException requestError(Long requestId, Long salonId) {
        Boolean exists = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM salon_subscription_requests WHERE id = ? AND salon_id = ?)",
                Boolean.class, requestId, salonId);
        return new BusinessRuleException(exists ? "SUBSCRIPTION_REQUEST_NOT_PENDING" : "SUBSCRIPTION_REQUEST_NOT_FOUND",
                exists ? "Yêu cầu không còn ở trạng thái chờ thanh toán" : "Không tìm thấy yêu cầu đăng ký của salon");
    }

    private static final RowMapper<SalonSubscriptionRequest> REQUEST_MAPPER = (rs, row) -> new SalonSubscriptionRequest(
            rs.getLong("id"), rs.getLong("salon_id"), rs.getString("salon_name"), rs.getString("tier"),
            rs.getBigDecimal("quoted_price"), rs.getString("status"), instant(rs, "created_at"), instant(rs, "reviewed_at"),
            rs.getString("review_note"), instant(rs, "starts_at"), instant(rs, "expires_at"), rs.getString("payment_qr_url"),
            instant(rs, "payment_reported_at"), rs.getString("payment_reference"), instant(rs, "payment_confirmed_at"),
            rs.getBigDecimal("received_amount"), rs.getObject("payos_order_code", Long.class),
            rs.getString("payos_payment_link_id"), rs.getString("payos_checkout_url"), rs.getString("payos_qr_code"));

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static Timestamp timestamp(Instant value) { return value == null ? null : Timestamp.from(value); }
}
