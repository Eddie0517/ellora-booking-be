package com.ellora.application.feature.subscription.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SubscriptionRequestResponse(Long id, Long salonId, String salonName, String tier,
        BigDecimal quotedPrice, String status, Instant createdAt, Instant reviewedAt, String reviewNote,
        Instant startsAt, Instant expiresAt, String paymentQrUrl, String transferContent,
        Instant paymentReportedAt, String paymentReference, Instant paymentConfirmedAt,
        BigDecimal receivedAmount, Long payosOrderCode, String payosPaymentLinkId,
        String payosCheckoutUrl, String payosQrCode) {}
