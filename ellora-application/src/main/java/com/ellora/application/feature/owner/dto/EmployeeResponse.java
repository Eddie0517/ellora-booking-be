package com.ellora.application.feature.owner.dto;

import java.time.Instant;

public record EmployeeResponse(
        Long id,
        Long salonId,
        String fullName,
        String phone,
        String avatarUrl,
        String bio,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
