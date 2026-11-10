package com.ellora.application.feature.subscription.dto;

import java.math.BigDecimal;
import java.util.List;

public record SubscriptionPlanResponse(
        String tier,
        String displayName,
        String badgeLabel,
        BigDecimal monthlyPrice,
        String currency,
        boolean enabled,
        boolean registrationAvailable,
        String description,
        List<String> features
) {
    public SubscriptionPlanResponse(String tier, String displayName, String badgeLabel, BigDecimal monthlyPrice,
            String currency, boolean enabled, boolean registrationAvailable) {
        this(tier, displayName, badgeLabel, monthlyPrice, currency, enabled, registrationAvailable, null, List.of());
    }
}
