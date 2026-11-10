package com.ellora.application.feature.owner.dto;

public record ResolvedCoordinatesResponse(
    Double latitude,
    Double longitude,
    String resolvedUrl
) {}
