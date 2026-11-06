package com.ellora.application.feature.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "Mã lịch hẹn không được để trống")
        Long bookingId,

        @NotNull(message = "Số sao đánh giá không được để trống")
        @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
        @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
        Integer rating,

        @Size(max = 2000, message = "Nội dung đánh giá không vượt quá 2000 ký tự")
        String comment
) {
}
