package com.ellora.application.feature.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record TransitionStatusRequest(
        @NotBlank(message = "Trạng thái mới không được để trống")
        String status,

        String cancellationReason,

        String note
) {}
