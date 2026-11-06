package com.ellora.application.feature.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewDetailsResponse(
        Long id,
        UUID customerId,
        String customerName,
        String customerAvatarUrl,
        Long salonId,
        Long bookingId,
        String serviceName,
        String employeeName,
        int rating,
        String comment,
        String salonReply,
        Instant salonRepliedAt,
        Instant createdAt
) {
}
