package com.ellora.application.feature.salon.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalonDetailsResponse(
    Long id,
    UUID ownerId,
    String name,
    String description,
    String address,
    String city,
    String district,
    String phone,
    String email,
    String logoUrl,
    String status,
    Double latitude,
    Double longitude,
    BigDecimal averageRating,
    Integer totalReviews
) {}
