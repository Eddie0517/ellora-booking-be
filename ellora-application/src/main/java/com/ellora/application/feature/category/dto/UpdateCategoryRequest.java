package com.ellora.application.feature.category.dto;

public record UpdateCategoryRequest(
        String name,
        String description,
        String iconUrl,
        Boolean active
) {
}
