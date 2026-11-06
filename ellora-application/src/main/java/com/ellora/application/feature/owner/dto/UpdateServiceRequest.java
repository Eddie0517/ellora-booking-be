package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateServiceRequest(
        Long categoryId,

        @NotBlank(message = "Tên dịch vụ không được để trống")
        @Size(max = 255, message = "Tên dịch vụ tối đa 255 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
        String description,

        @NotNull(message = "Giá dịch vụ không được để trống")
        @Min(value = 0, message = "Giá dịch vụ không được âm")
        BigDecimal price,

        @NotNull(message = "Thời gian thực hiện không được để trống")
        @Min(value = 5, message = "Thời gian thực hiện tối thiểu 5 phút")
        Integer durationMinutes,

        String imageUrl,

        Boolean active
) {
}
