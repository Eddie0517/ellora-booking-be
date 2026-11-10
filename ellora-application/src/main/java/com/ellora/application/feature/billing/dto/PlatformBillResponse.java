package com.ellora.application.feature.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PlatformBillResponse(
        Long salonId,
        String salonName,
        String month,
        BigDecimal gross,
        BigDecimal percent,
        BigDecimal fee,
        BigDecimal paid,
        BigDecimal due,
        LocalDate dueDate,
        String status,
        String transferContent,
        String qrUrl,
        Instant paidAt,
        Instant remindedAt,
        String payosCheckoutUrl,
        String payosQrCode,
        Long payosOrderCode
) {
    public PlatformBillResponse(Long salonId, String salonName, String month, BigDecimal gross,
                                BigDecimal percent, BigDecimal fee, BigDecimal paid, BigDecimal due,
                                LocalDate dueDate, String status, String transferContent, String qrUrl,
                                Instant paidAt, Instant remindedAt) {
        this(salonId, salonName, month, gross, percent, fee, paid, due, dueDate, status,
                transferContent, qrUrl, paidAt, remindedAt, null, null, null);
    }
}
