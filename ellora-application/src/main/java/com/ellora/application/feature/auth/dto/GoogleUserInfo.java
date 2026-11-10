package com.ellora.application.feature.auth.dto;

public record GoogleUserInfo(
        String googleId,
        String email,
        String fullName,
        String avatarUrl,
        boolean emailVerified
) {}
