package com.ellora.application.feature.admin.dto;

import java.util.List;

public record CreateSalonChangeRequest(
        String kind,
        SalonChangeFieldsDto proposed,
        String proposedLogoUrl,
        String proposedCoverUrl,
        List<String> proposedImageUrls
) {
}
