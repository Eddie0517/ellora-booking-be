package com.ellora.application.feature.salon.dto;

import java.math.BigDecimal;

public record SalonSummaryResponse(
    Long id,
    String name,
    String address,
    String city,
    String district,
    String phone,
    String logoUrl,
    BigDecimal averageRating,
    Integer totalReviews
) {}
