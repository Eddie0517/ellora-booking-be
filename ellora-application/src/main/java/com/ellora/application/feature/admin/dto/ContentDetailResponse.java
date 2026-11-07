package com.ellora.application.feature.admin.dto;

import java.util.List;

public record ContentDetailResponse(
        Long id,
        String name,
        List<ContentPhotoItem> photos,
        List<String> gallery
) {}
