package com.ellora.application.feature.auth.dto;

import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String email,
    String fullName,
    String phone,
    String avatarUrl,
    String role,
    boolean emailVerified
) {}
