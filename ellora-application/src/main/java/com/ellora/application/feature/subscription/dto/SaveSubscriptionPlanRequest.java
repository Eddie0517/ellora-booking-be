package com.ellora.application.feature.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record SaveSubscriptionPlanRequest(@DecimalMin("0.01") BigDecimal monthlyPrice, boolean enabled) {}
