package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveMapsUrlRequest(
    @NotBlank(message = "Đường dẫn Google Maps không được để trống")
    String url
) {}
