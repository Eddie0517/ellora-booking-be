package com.ellora.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record SalonSubscriptionRequest(
        Long id, Long salonId, String salonName, String tier, BigDecimal quotedPrice, String status,
        Instant createdAt, Instant reviewedAt, String reviewNote, Instant startsAt, Instant expiresAt,
        String paymentQrUrl, Instant paymentReportedAt, String paymentReference,
        Instant paymentConfirmedAt, BigDecimal receivedAmount, Long payosOrderCode,
        String payosPaymentLinkId, String payosCheckoutUrl, String payosQrCode) {}
