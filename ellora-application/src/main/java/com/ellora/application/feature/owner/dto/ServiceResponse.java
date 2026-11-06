package com.ellora.application.feature.owner.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceResponse(
        Long id,
        Long salonId,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        String imageUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
