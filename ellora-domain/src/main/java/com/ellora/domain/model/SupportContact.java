package com.ellora.domain.model;

public record SupportContact(
        Long id,
        String email,
        String phone,
        String facebookUrl
) {
}
