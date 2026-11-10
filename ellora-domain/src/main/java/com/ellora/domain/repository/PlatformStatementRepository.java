package com.ellora.domain.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PlatformStatementRepository {

    record StatementRecord(
            Long id,
            Long salonId,
            LocalDate billingMonth,
            BigDecimal paidAmount,
            Instant paidAt,
            UUID confirmedBy,
            Instant remindedAt
    ) {}

    record StatementPaymentRecord(
            Long id,
            Long salonId,
            String billingMonth,
            BigDecimal amount,
            Long payosOrderCode,
            String payosPaymentLinkId,
            String payosCheckoutUrl,
            String payosQrCode,
            String status,
            String paymentReference,
            Instant paymentReportedAt,
            Instant createdAt
    ) {}

    Optional<StatementRecord> findBySalonIdAndMonth(Long salonId, LocalDate month);

    Optional<StatementPaymentRecord> findPendingPayment(Long salonId, String billingMonth);

    void savePayment(Long salonId, String billingMonth, BigDecimal amount, Long orderCode,
                     String paymentLinkId, String checkoutUrl, String qrCode);

    void markPaymentPaid(Long orderCode, String paymentReference, Instant reportedAt);

    void recordPayment(Long salonId, LocalDate month, BigDecimal additionalPaid, UUID confirmedBy);

    void recordReminder(Long salonId, LocalDate month);
}
