package com.ellora.application.feature.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingDisputeResponse(
        Long id, Long bookingId, Long completionVersion, String status, String reason,
        Instant createdAt, Instant resolvedAt, String adminNote,
        String customerName, String customerPhone, String customerEmail,
        Long salonId, String salonName, String salonPhone, String salonEmail, String salonAddress,
        String ownerName, String ownerPhone, String ownerEmail,
        String serviceName, BigDecimal servicePrice, Instant scheduledAt, Integer durationMinutes,
        String employeeName, String bookingStatus, Instant completedAt
) {}
