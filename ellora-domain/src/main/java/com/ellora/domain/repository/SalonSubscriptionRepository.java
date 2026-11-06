package com.ellora.domain.repository;

import com.ellora.domain.model.SalonSubscriptionPlan;
import com.ellora.domain.model.SalonSubscriptionRequest;
import com.ellora.domain.model.SalonSubscriptionSnapshot;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface SalonSubscriptionRepository {
    List<SalonSubscriptionPlan> findPlans();
    Optional<SalonSubscriptionPlan> findPlan(String tier);
    SalonSubscriptionPlan savePlan(String tier, BigDecimal monthlyPrice, boolean enabled, UUID adminId);
    Optional<Long> findSalonIdByOwner(UUID ownerId);
    Optional<SalonSubscriptionSnapshot> findCurrentSubscription(Long salonId);
    Optional<SalonSubscriptionRequest> findPendingRequest(Long salonId);
    Optional<SalonSubscriptionRequest> findRequest(Long requestId);
    List<SalonSubscriptionRequest> findOwnerRequests(Long salonId, int offset, int limit);
    long countOwnerRequests(Long salonId);
    SalonSubscriptionRequest createRequest(Long salonId, String tier, BigDecimal quotedPrice);
    Long nextPayosOrderCode();
    SalonSubscriptionRequest savePayosPayment(Long salonId, Long requestId, Long orderCode, String paymentLinkId, String checkoutUrl, String qrCode);
    void confirmPayosPayment(Long orderCode, String paymentLinkId, BigDecimal amount, String reference);
    void resetPayosPayment(Long requestId, Long orderCode);
    List<SalonSubscriptionRequest> findAdminRequests(String status, int offset, int limit);
    long countAdminRequests(String status);
    SalonSubscriptionRequest decideRequest(Long requestId, UUID adminId, String status, String note,
            Instant startsAt, Instant expiresAt, BigDecimal receivedAmount);
}
