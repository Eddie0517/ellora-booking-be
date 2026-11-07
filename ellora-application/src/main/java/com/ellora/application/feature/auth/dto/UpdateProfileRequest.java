package com.ellora.application.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(min = 2, max = 150, message = "Họ và tên từ 2 đến 150 ký tự")
        String fullName,

        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        String phone,

        String avatarUrl
) {
    public UpdateProfileRequest(String fullName, String phone) {
        this(fullName, phone, null);
    }
}
