package com.ellora.application.feature.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống")
        String name,

        String description,

        String iconUrl
) {
}
