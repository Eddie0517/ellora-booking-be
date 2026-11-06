package com.ellora.application.feature.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record DecideSubscriptionRequest(@NotBlank String status, String note) {}
