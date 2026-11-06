package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOwnerSalonRequest(
        @NotBlank(message = "Tên salon không được để trống")
        @Size(max = 255, message = "Tên salon tối đa 255 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
        String description,

        @NotBlank(message = "Địa chỉ không được để trống")
        String address,

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        String city,

        @NotBlank(message = "Quận/Huyện không được để trống")
        String district,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        String phone,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        String logoUrl
) {
}
