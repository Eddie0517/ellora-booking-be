package com.ellora.application.feature.salon.dto;

import java.math.BigDecimal;

public record NailServiceResponse(
    Long id,
    Long salonId,
    Long categoryId,
    String name,
    String description,
    BigDecimal price,
    Integer durationMinutes,
    String imageUrl,
    boolean active
) {}
