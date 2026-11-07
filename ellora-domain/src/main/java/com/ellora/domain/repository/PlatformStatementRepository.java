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

    Optional<StatementRecord> findBySalonIdAndMonth(Long salonId, LocalDate month);

    void recordPayment(Long salonId, LocalDate month, BigDecimal additionalPaid, UUID confirmedBy);

    void recordReminder(Long salonId, LocalDate month);
}
