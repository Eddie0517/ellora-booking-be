package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
        @NotBlank(message = "Họ tên nhân viên không được để trống")
        @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
        String fullName,

        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        String phone,

        String avatarUrl,

        @Size(max = 1000, message = "Giới thiệu tối đa 1000 ký tự")
        String bio,

        Boolean active
) {
}
