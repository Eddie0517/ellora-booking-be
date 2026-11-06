package com.ellora.domain.model;

import java.math.BigDecimal;

public record SalonSubscriptionPlan(String tier, BigDecimal monthlyPrice, boolean enabled) {}
