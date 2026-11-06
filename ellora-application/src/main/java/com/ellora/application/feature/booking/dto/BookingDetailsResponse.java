package com.ellora.application.feature.booking.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingDetailsResponse(
        Long id,
        UUID customerId,
        String walkInName,
        String guestEmail,
        String walkInPhone,
        Long salonId,
        Long serviceId,
        Long employeeId,
        Instant scheduledAt,
        Integer durationMinutes,
        BigDecimal servicePriceSnapshot,
        String status,
        String customerNote,
        String salonNote,
        String cancellationReason,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        String customerName,
        String customerPhone,
        String salonName,
        String serviceName,
        String completionConfirmation,
        Long completionVersion
) {
    public BookingDetailsResponse(Long id, UUID customerId, String walkInName, String guestEmail,
                                  String walkInPhone, Long salonId, Long serviceId, Long employeeId,
                                  Instant scheduledAt, Integer durationMinutes, BigDecimal servicePriceSnapshot,
                                  String status, String customerNote, String salonNote, String cancellationReason,
                                  Instant completedAt, Instant createdAt, Instant updatedAt) {
        this(id, customerId, walkInName, guestEmail, walkInPhone, salonId, serviceId, employeeId,
                scheduledAt, durationMinutes, servicePriceSnapshot, status, customerNote, salonNote,
                cancellationReason, completedAt, createdAt, updatedAt, null, null, null, null, null, null);
    }
}
