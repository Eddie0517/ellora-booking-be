package com.ellora.application.feature.category.dto;

public record ServiceCategoryResponse(
        Long id,
        String name,
        String description,
        String iconUrl,
        boolean active
) {
}
