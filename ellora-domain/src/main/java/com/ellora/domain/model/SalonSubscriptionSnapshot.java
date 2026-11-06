package com.ellora.domain.model;

import java.time.Instant;

public record SalonSubscriptionSnapshot(Long salonId, String tier, Instant startsAt, Instant expiresAt) {}
