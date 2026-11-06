package com.ellora.application.feature.owner.dto;

import java.math.BigDecimal;

public record OwnerDashboardSummaryResponse(
        long totalBookings,
        long pendingBookings,
        long confirmedBookings,
        long completedBookings,
        BigDecimal monthlyRevenue,
        long monthlyCustomers,
        double averageRating
) {
}
