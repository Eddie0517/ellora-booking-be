package com.ellora.application.feature.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long totalSalons,
        long pendingSalons,
        long activeSalons,
        long totalUsers,
        long totalBookings,
        BigDecimal totalRevenue
) {
}
