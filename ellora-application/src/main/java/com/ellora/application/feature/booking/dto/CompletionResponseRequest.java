package com.ellora.application.feature.booking.dto;

import jakarta.validation.constraints.NotNull;

public record CompletionResponseRequest(
        @NotNull Boolean completed,
        @NotNull Long completionVersion,
        String reason
) {}
