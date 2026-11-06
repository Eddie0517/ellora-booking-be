package com.ellora.application.feature.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserProfileResponse user
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserProfileResponse user) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
