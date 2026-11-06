package com.ellora.application.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mã xác thực không được để trống")
        @Pattern(regexp = "^\\d{6}$", message = "Mã xác thực phải bao gồm 6 chữ số")
        String code
) {
}
