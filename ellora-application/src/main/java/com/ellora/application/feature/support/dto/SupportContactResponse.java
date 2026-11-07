package com.ellora.application.feature.support.dto;

public record SupportContactResponse(
        String email,
        String phone,
        String facebookUrl
) {
}
