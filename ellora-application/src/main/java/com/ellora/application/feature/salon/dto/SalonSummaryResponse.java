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
    Integer totalReviews,
    String subscriptionTier,
    Double latitude,
    Double longitude,
    Double distanceKm
) {
    public SalonSummaryResponse(Long id, String name, String address, String city, String district,
                                String phone, String logoUrl, BigDecimal averageRating, Integer totalReviews,
                                String subscriptionTier) {
        this(id, name, address, city, district, phone, logoUrl, averageRating, totalReviews, subscriptionTier, null, null, null);
    }

    public SalonSummaryResponse(Long id, String name, String address, String city, String district,
                                String phone, String logoUrl, BigDecimal averageRating, Integer totalReviews) {
        this(id, name, address, city, district, phone, logoUrl, averageRating, totalReviews, null, null, null, null);
    }
}
