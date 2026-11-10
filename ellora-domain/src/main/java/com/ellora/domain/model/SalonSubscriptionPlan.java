package com.ellora.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record SalonSubscriptionPlan(
        String tier,
        BigDecimal monthlyPrice,
        boolean enabled,
        String description,
        List<String> features
) {
    public SalonSubscriptionPlan(String tier, BigDecimal monthlyPrice, boolean enabled) {
        this(tier, monthlyPrice, enabled, null, List.of());
    }
}
