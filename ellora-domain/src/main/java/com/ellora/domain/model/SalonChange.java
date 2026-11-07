package com.ellora.domain.model;

import java.time.Instant;
import java.util.UUID;

public record SalonChange(
        Long id,
        Long salonId,
        String kind,
        String proposedJson,
        String previousLogoUrl,
        String proposedLogoUrl,
        String previousImageUrlsJson,
        String proposedImageUrlsJson,
        String status,
        Instant reviewedAt,
        UUID reviewedBy,
        Instant submittedAt
) {
}
