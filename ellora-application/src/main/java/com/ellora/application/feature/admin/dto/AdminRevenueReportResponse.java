package com.ellora.application.feature.admin.dto;

import java.util.List;

public record AdminRevenueReportResponse(
        String from,
        String to,
        String groupBy,
        Long gross,
        Long fee,
        Integer completedBookings,
        Integer estimatedBookings,
        List<AdminRevenuePointResponse> points
) {
}
