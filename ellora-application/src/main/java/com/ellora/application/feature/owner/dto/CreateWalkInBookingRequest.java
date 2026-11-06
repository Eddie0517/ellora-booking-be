package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateWalkInBookingRequest(
        @NotNull Long serviceId,
        Long employeeId,
        @NotNull Instant scheduledAt,
        @NotBlank String customerName,
        @NotBlank String customerPhone,
        String customerNote) {}
