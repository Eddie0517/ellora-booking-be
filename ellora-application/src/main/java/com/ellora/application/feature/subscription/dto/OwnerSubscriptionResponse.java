package com.ellora.application.feature.subscription.dto;

import java.time.Instant;

public record OwnerSubscriptionResponse(Long salonId, String effectiveTier, String badgeLabel,
        Instant startsAt, Instant expiresAt, SubscriptionRequestResponse pendingRequest) {}
