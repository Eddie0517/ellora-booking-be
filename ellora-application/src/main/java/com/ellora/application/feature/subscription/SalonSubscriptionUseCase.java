package com.ellora.application.feature.subscription;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.common.port.PaymentGateway;
import com.ellora.application.feature.subscription.dto.CreateSubscriptionRequest;
import com.ellora.application.feature.subscription.dto.DecideSubscriptionRequest;
import com.ellora.application.feature.subscription.dto.OwnerSubscriptionResponse;
import com.ellora.application.feature.subscription.dto.SaveSubscriptionPlanRequest;
import com.ellora.application.feature.subscription.dto.SubscriptionPlanResponse;
import com.ellora.application.feature.subscription.dto.SubscriptionRequestResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.SalonSubscriptionPlan;
import com.ellora.domain.model.SalonSubscriptionRequest;
import com.ellora.domain.model.SalonSubscriptionSnapshot;
import com.ellora.domain.repository.SalonSubscriptionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalonSubscriptionUseCase {
    private static final int MAX_PAGE_SIZE = 100;
    private final SalonSubscriptionRepository subscriptions;
    private final PaymentGateway paymentGateway;
    @org.springframework.beans.factory.annotation.Value("${app.payos.checksum-key:}")
    private String payosChecksumKey;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> publicPlans() {
        return subscriptions.findPlans().stream().map(this::toPlanResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> adminPlans() { return publicPlans(); }

    @Transactional
    public SubscriptionPlanResponse savePlan(UUID adminId, String tier, SaveSubscriptionPlanRequest request) {
        String normalizedTier = normalizeTier(tier);
        if (request.enabled() && request.monthlyPrice() == null) {
            throw new BusinessRuleException("INVALID_PLAN_PRICE", "Gói đang mở bán phải có giá tháng");
        }
        return toPlanResponse(subscriptions.savePlan(normalizedTier, request.monthlyPrice(), request.enabled(),
                request.description(), request.features(), adminId));
    }

    @Transactional(readOnly = true)
    public OwnerSubscriptionResponse ownerSubscription(UUID ownerId) {
        Long salonId = ownerSalon(ownerId);
        SalonSubscriptionSnapshot current = subscriptions.findCurrentSubscription(salonId).orElse(null);
        String tier = current != null && current.tier() != null && current.expiresAt() != null
                && current.expiresAt().isAfter(Instant.now()) ? current.tier() : null;
        SalonSubscriptionRequest pending = subscriptions.findPendingRequest(salonId).orElse(null);
        return new OwnerSubscriptionResponse(salonId, tier, badge(tier),
                tier == null ? null : current.startsAt(), tier == null ? null : current.expiresAt(), toRequestResponse(pending));
    }

    @Transactional(readOnly = true)
    public PagedResult<SubscriptionRequestResponse> ownerRequests(UUID ownerId, int page, int pageSize) {
        Long salonId = ownerSalon(ownerId);
        int safePage = Math.max(page, 0);
        int safeSize = safePageSize(pageSize);
        List<SubscriptionRequestResponse> items = subscriptions.findOwnerRequests(salonId, safePage * safeSize, safeSize)
                .stream().map(this::toRequestResponse).toList();
        return PagedResult.of(items, safePage, safeSize, subscriptions.countOwnerRequests(salonId));
    }

    @Transactional
    public SubscriptionRequestResponse createRequest(UUID ownerId, CreateSubscriptionRequest request) {
        Long salonId = ownerSalon(ownerId);
        String tier = normalizeTier(request.tier());
        if (subscriptions.findPendingRequest(salonId).isPresent()) {
            throw new BusinessRuleException("SUBSCRIPTION_REQUEST_PENDING", "Salon đang có yêu cầu gói chờ xử lý");
        }
        SalonSubscriptionPlan plan = subscriptions.findPlan(tier)
                .orElseThrow(() -> new BusinessRuleException("SUBSCRIPTION_PLAN_NOT_FOUND", "Không tìm thấy gói đăng ký"));
        if (!plan.enabled() || plan.monthlyPrice() == null || plan.monthlyPrice().signum() <= 0) {
            throw new BusinessRuleException("SUBSCRIPTION_PLAN_UNAVAILABLE", "Gói này hiện không mở đăng ký");
        }
        if (request.expectedPrice() == null || request.expectedPrice().compareTo(plan.monthlyPrice()) != 0) {
            throw new BusinessRuleException("SUBSCRIPTION_PRICE_CHANGED", "Giá gói đã thay đổi, vui lòng tải lại bảng giá");
        }
        return toRequestResponse(subscriptions.createRequest(salonId, tier, plan.monthlyPrice()));
    }

    @Transactional
    public SubscriptionRequestResponse preparePayment(UUID ownerId, Long requestId) {
        Long salonId = ownerSalon(ownerId);
        SalonSubscriptionRequest request = subscriptions.findRequest(requestId)
                .filter(value -> value.salonId().equals(salonId) && "PENDING".equals(value.status()))
                .orElseThrow(() -> new BusinessRuleException("SUBSCRIPTION_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu đăng ký của salon"));
        if (request.payosCheckoutUrl() != null && !request.payosCheckoutUrl().isBlank()) return toRequestResponse(request);
        long orderCode = subscriptions.nextPayosOrderCode();
        long amount;
        try { amount = request.quotedPrice().longValueExact(); }
        catch (ArithmeticException e) { throw new BusinessRuleException("INVALID_SUBSCRIPTION_PRICE", "Giá gói không hợp lệ cho thanh toán"); }
        PaymentGateway.PaymentLink link = paymentGateway.createSubscriptionLink(orderCode, amount, "SUB" + request.id());
        return toRequestResponse(subscriptions.savePayosPayment(salonId, requestId, orderCode,
                link.paymentLinkId(), link.checkoutUrl(), link.qrCode()));
    }

    @Transactional
    public SubscriptionRequestResponse syncOwnerPayment(UUID ownerId, Long requestId) {
        Long salonId = ownerSalon(ownerId);
        SalonSubscriptionRequest request = subscriptions.findRequest(requestId)
                .filter(value -> value.salonId().equals(salonId))
                .orElseThrow(() -> new BusinessRuleException("SUBSCRIPTION_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu đăng ký của salon"));
        return syncPayment(request);
    }

    @Transactional
    public SubscriptionRequestResponse syncAdminPayment(Long requestId) {
        SalonSubscriptionRequest request = subscriptions.findRequest(requestId)
                .orElseThrow(() -> new BusinessRuleException("SUBSCRIPTION_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu gói"));
        return syncPayment(request);
    }

    private SubscriptionRequestResponse syncPayment(SalonSubscriptionRequest request) {
        if (request.payosOrderCode() == null || request.payosPaymentLinkId() == null)
            throw new BusinessRuleException("PAYMENT_NOT_STARTED", "Yêu cầu chưa tạo liên kết PayOS");
        if (request.paymentReportedAt() != null || !"PENDING".equals(request.status())) return toRequestResponse(request);
        PaymentGateway.PaymentStatus status = paymentGateway.getSubscriptionPayment(request.payosOrderCode());
        long quoted;
        try { quoted = request.quotedPrice().longValueExact(); }
        catch (ArithmeticException e) { throw new BusinessRuleException("INVALID_SUBSCRIPTION_PRICE", "Giá gói không hợp lệ"); }
        if (status.orderCode() != request.payosOrderCode() || !request.payosPaymentLinkId().equals(status.paymentLinkId())
                || status.amount() != quoted)
            throw new BusinessRuleException("PAYMENT_MISMATCH", "Thông tin thanh toán PayOS không khớp yêu cầu");
        if ("PAID".equals(status.status())) {
            if (status.amountPaid() != quoted) throw new BusinessRuleException("PAYMENT_MISMATCH", "Số tiền PayOS xác nhận không khớp giá gói");
            subscriptions.confirmPayosPayment(request.payosOrderCode(), request.payosPaymentLinkId(),
                    request.quotedPrice(), status.reference());
        } else if ("CANCELLED".equals(status.status()) || "EXPIRED".equals(status.status())) {
            subscriptions.resetPayosPayment(request.id(), request.payosOrderCode());
        }
        return toRequestResponse(subscriptions.findRequest(request.id()).orElseThrow());
    }

    @Transactional
    public void handlePayosWebhook(Map<String, Object> payload) {
        if (payosChecksumKey == null || payosChecksumKey.isBlank()) throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "PayOS chưa được cấu hình");
        Object rawData = payload.get("data");
        Object rawSignature = payload.get("signature");
        if (!(rawData instanceof Map<?, ?> data) || !(rawSignature instanceof String signature) || !validPayosSignature(data, signature))
            throw new BusinessRuleException("INVALID_PAYMENT_SIGNATURE", "Chữ ký webhook PayOS không hợp lệ");
        if (!"00".equals(String.valueOf(payload.get("code"))) || !Boolean.TRUE.equals(payload.get("success"))
                || !"00".equals(String.valueOf(data.get("code")))) return;
        long orderCode = Long.parseLong(String.valueOf(data.get("orderCode")));
        long amount = Long.parseLong(String.valueOf(data.get("amount")));
        subscriptions.confirmPayosPayment(orderCode, String.valueOf(data.get("paymentLinkId")),
                BigDecimal.valueOf(amount), data.get("reference") == null ? null : String.valueOf(data.get("reference")));
    }

    private boolean validPayosSignature(Map<?, ?> data, String provided) {
        try {
            TreeMap<String, Object> sorted = new TreeMap<>();
            data.forEach((key, value) -> sorted.put(String.valueOf(key), value));
            String canonical = sorted.entrySet().stream().map(entry -> entry.getKey() + "=" + signatureValue(entry.getValue()))
                    .collect(java.util.stream.Collectors.joining("&"));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(payosChecksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = java.util.HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), provided.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) { return false; }
    }

    private String signatureValue(Object value) {
        if (value == null || "null".equals(value) || "undefined".equals(value)) return "";
        if (value instanceof List<?> list) return list.stream().map(item -> item instanceof Map<?, ?> map
                ? new java.util.TreeMap<>(map) : item).toList().toString();
        return String.valueOf(value);
    }

    @Transactional(readOnly = true)
    public PagedResult<SubscriptionRequestResponse> adminRequests(String status, int page, int pageSize) {
        int safePage = Math.max(page, 0);
        int safeSize = safePageSize(pageSize);
        String filter = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        if (filter != null && !List.of("PENDING", "APPROVED", "REJECTED").contains(filter)) {
            throw new BusinessRuleException("INVALID_SUBSCRIPTION_STATUS", "Trạng thái yêu cầu không hợp lệ");
        }
        List<SubscriptionRequestResponse> items = subscriptions.findAdminRequests(filter, safePage * safeSize, safeSize)
                .stream().map(this::toRequestResponse).toList();
        return PagedResult.of(items, safePage, safeSize, subscriptions.countAdminRequests(filter));
    }

    @Transactional
    public SubscriptionRequestResponse decide(UUID adminId, Long requestId, DecideSubscriptionRequest request) {
        if (adminId == null) throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập quản trị viên");
        String status = request.status().trim().toUpperCase(Locale.ROOT);
        if (!List.of("APPROVED", "REJECTED").contains(status)) {
            throw new BusinessRuleException("INVALID_SUBSCRIPTION_STATUS", "Quyết định không hợp lệ");
        }
        SalonSubscriptionRequest current = subscriptions.findRequest(requestId)
                .orElseThrow(() -> new BusinessRuleException("SUBSCRIPTION_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu gói"));
        String note = request.note() == null ? null : request.note().trim();
        if ("REJECTED".equals(status) && (note == null || note.isBlank())) {
            throw new BusinessRuleException("REJECTION_NOTE_REQUIRED", "Cần ghi lý do từ chối yêu cầu");
        }
        if ("REJECTED".equals(status) && current.payosOrderCode() != null && current.paymentReportedAt() != null) {
            throw new BusinessRuleException("PAID_REQUEST_CANNOT_BE_REJECTED", "Yêu cầu đã thanh toán cần được xử lý hoàn tiền trước khi từ chối");
        }
        BigDecimal receivedAmount = null;
        Instant startsAt = null;
        Instant expiresAt = null;
        if ("APPROVED".equals(status)) {
            if (current.paymentReportedAt() == null || current.payosOrderCode() == null
                    || current.receivedAmount() == null || current.receivedAmount().compareTo(current.quotedPrice()) != 0) {
                throw new BusinessRuleException("PAYMENT_NOT_CONFIRMED", "Chỉ kích hoạt sau khi PayOS xác nhận đã nhận đủ tiền");
            }
            receivedAmount = current.receivedAmount();
            startsAt = Instant.now();
            expiresAt = startsAt.atZone(java.time.ZoneOffset.UTC).plusMonths(1).toInstant();
        }
        return toRequestResponse(subscriptions.decideRequest(requestId, adminId, status, note, startsAt, expiresAt, receivedAmount));
    }

    private Long ownerSalon(UUID ownerId) {
        if (ownerId == null) throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập chủ salon");
        return subscriptions.findSalonIdByOwner(ownerId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm"));
    }

    private int safePageSize(int pageSize) { return Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE)); }

    private String normalizeTier(String tier) {
        String normalized = tier == null ? "" : tier.trim().toUpperCase(Locale.ROOT);
        if (!List.of("STANDARD", "VIP").contains(normalized)) {
            throw new BusinessRuleException("INVALID_SUBSCRIPTION_TIER", "Gói đăng ký không hợp lệ");
        }
        return normalized;
    }

    @Transactional
    public void cancelPendingRequest(UUID ownerId, Long requestId) {
        Long salonId = ownerSalon(ownerId);
        SalonSubscriptionRequest request = subscriptions.findRequest(requestId)
                .filter(value -> value.salonId().equals(salonId) && "PENDING".equals(value.status()))
                .orElseThrow(() -> new BusinessRuleException("SUBSCRIPTION_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu đăng ký của salon"));
        if (request.paymentReportedAt() != null) {
            throw new BusinessRuleException("PAID_REQUEST_CANNOT_BE_CANCELLED", "Yêu cầu đã thanh toán không thể hủy");
        }
        subscriptions.cancelRequest(requestId);
    }

    private String badge(String tier) { return tier == null ? "" : "VIP".equals(tier) ? "VIP" : "Standard"; }

    private SubscriptionPlanResponse toPlanResponse(SalonSubscriptionPlan plan) {
        String label = badge(plan.tier());
        String defaultDesc = "VIP".equals(plan.tier())
                ? "Gói cao cấp giúp salon bứt phá doanh thu, ưu tiên hiển thị hàng đầu và tiếp cận khách hàng tiềm năng."
                : "Dành cho các cơ sở salon bắt đầu tiếp cận khách hàng trực tuyến với đầy đủ tính năng vận hành cơ bản.";
        List<String> defaultFeatures = "VIP".equals(plan.tier())
                ? List.of(
                        "Tất cả quyền lợi của gói Standard",
                        "Ghim nổi bật (Featured) trên trang khám phá và tìm kiếm",
                        "Huy hiệu VIP Nổi bật sang trọng trên bản đồ và hồ sơ",
                        "Ưu tiên hiển thị khi khách hàng tìm kiếm địa điểm gần nhất",
                        "Báo cáo phân tích doanh thu, hiệu suất thợ và lịch hẹn chi tiết",
                        "Tạo chương trình ưu đãi và mã giảm giá thu hút khách",
                        "Hỗ trợ chăm sóc khách hàng ưu tiên 24/7 từ chuyên viên Ellora"
                )
                : List.of(
                        "Quản lý thông tin và hồ sơ salon trên hệ thống",
                        "Tiếp nhận và quản trị lịch hẹn khách hàng trực tuyến",
                        "Quản lý danh sách dịch vụ làm móng và giá niêm yết",
                        "Quản lý danh sách nhân sự và thợ làm móng",
                        "Hiển thị vị trí trên bản đồ tìm kiếm",
                        "Hỗ trợ kỹ thuật tiêu chuẩn từ đội ngũ Ellora"
                );
        String desc = plan.description() != null && !plan.description().isBlank() ? plan.description() : defaultDesc;
        List<String> feats = plan.features() != null && !plan.features().isEmpty() ? plan.features() : defaultFeatures;
        return new SubscriptionPlanResponse(plan.tier(), label, "VIP".equals(plan.tier()) ? "Nổi bật" : "Cơ bản",
                plan.monthlyPrice(), "VND", plan.enabled(), plan.enabled() && plan.monthlyPrice() != null,
                desc, feats);
    }

    private SubscriptionRequestResponse toRequestResponse(SalonSubscriptionRequest request) {
        if (request == null) return null;
        return new SubscriptionRequestResponse(request.id(), request.salonId(), request.salonName(), request.tier(),
                request.quotedPrice(), request.status(), request.createdAt(), request.reviewedAt(), request.reviewNote(),
                request.startsAt(), request.expiresAt(), request.paymentQrUrl(), "ELLORA-SUB-" + request.id(),
                request.payosOrderCode() == null ? null : request.paymentReportedAt(),
                request.payosOrderCode() == null ? null : request.paymentReference(), request.paymentConfirmedAt(),
                request.payosOrderCode() == null ? null : request.receivedAmount(),
                request.payosOrderCode(), request.payosPaymentLinkId(), request.payosCheckoutUrl(), request.payosQrCode());
    }
}
