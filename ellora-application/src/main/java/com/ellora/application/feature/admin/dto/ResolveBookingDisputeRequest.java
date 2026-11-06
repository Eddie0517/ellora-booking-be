package com.ellora.application.feature.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveBookingDisputeRequest(@NotBlank String outcome, @NotBlank String note) {}
