package com.ellora.application.feature.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

public record SaveSubscriptionPlanRequest(
        @DecimalMin("0.01") BigDecimal monthlyPrice,
        boolean enabled,
        String description,
        List<String> features
) {
    public SaveSubscriptionPlanRequest(BigDecimal monthlyPrice, boolean enabled) {
        this(monthlyPrice, enabled, null, List.of());
    }
}
