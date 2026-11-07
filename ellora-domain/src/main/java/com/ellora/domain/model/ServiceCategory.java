package com.ellora.domain.model;

public record ServiceCategory(
        Long id,
        String name,
        String description,
        String iconUrl,
        boolean active
) {
}
