package com.ellora.application.feature.admin.dto;

public record SalonChangeFieldsDto(
        String name,
        String description,
        String address,
        String city,
        String district,
        String phone,
        String email,
        Double latitude,
        Double longitude
) {
}
