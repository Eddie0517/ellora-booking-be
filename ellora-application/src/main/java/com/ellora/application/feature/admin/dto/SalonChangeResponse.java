package com.ellora.application.feature.admin.dto;

import java.time.Instant;
import java.util.List;

public record SalonChangeResponse(
        Long id,
        Long salonId,
        String salonName,
        String kind,
        String status,
        Instant submittedAt,
        SalonChangeFieldsDto current,
        SalonChangeFieldsDto proposed,
        String currentLogoUrl,
        String proposedLogoUrl,
        List<String> currentImageUrls,
        List<String> proposedImageUrls
) {
}
