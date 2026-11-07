package com.ellora.application.feature.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminEditReviewRequest(
        @Min(value = 1, message = "Điểm đánh giá tối thiểu 1 sao")
        @Max(value = 5, message = "Điểm đánh giá tối đa 5 sao")
        int rating,

        @Size(max = 1000, message = "Nhận xét tối đa 1000 ký tự")
        String comment
) {}
