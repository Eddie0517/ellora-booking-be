package com.ellora.application.feature.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String role,
        boolean enabled,
        boolean locked,
        Instant emailVerifiedAt,
        Instant createdAt
) {
}
