package com.ellora.application.feature.admin.dto;

public record AdminRevenuePointResponse(
        String date,
        Long gross,
        Long fee,
        Integer bookings
) {
}
