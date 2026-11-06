package com.ellora.application.feature.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateSubscriptionRequest(@NotBlank String tier, @NotNull @DecimalMin("0.01") BigDecimal expectedPrice) {}
