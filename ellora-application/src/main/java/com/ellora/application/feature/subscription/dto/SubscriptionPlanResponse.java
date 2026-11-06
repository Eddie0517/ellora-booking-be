package com.ellora.application.feature.subscription.dto;

import java.math.BigDecimal;

public record SubscriptionPlanResponse(String tier, String displayName, String badgeLabel, BigDecimal monthlyPrice,
        String currency, boolean enabled, boolean registrationAvailable) {}
