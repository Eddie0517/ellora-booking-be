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
    Integer totalReviews,
    String subscriptionTier,
    java.util.List<String> imageUrls
) {
    public SalonDetailsResponse(Long id, UUID ownerId, String name, String description, String address,
                                String city, String district, String phone, String email, String logoUrl,
                                String status, Double latitude, Double longitude, BigDecimal averageRating,
                                Integer totalReviews) {
        this(id, ownerId, name, description, address, city, district, phone, email, logoUrl,
                status, latitude, longitude, averageRating, totalReviews, null, java.util.Collections.emptyList());
    }

    public SalonDetailsResponse(Long id, UUID ownerId, String name, String description, String address,
                                String city, String district, String phone, String email, String logoUrl,
                                String status, Double latitude, Double longitude, BigDecimal averageRating,
                                Integer totalReviews, String subscriptionTier) {
        this(id, ownerId, name, description, address, city, district, phone, email, logoUrl,
                status, latitude, longitude, averageRating, totalReviews, subscriptionTier, java.util.Collections.emptyList());
    }
}
