package com.ellora.application.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng RFC 5322")
    String email,

    @NotBlank(message = "Mật khẩu không được để trống")
    String password
) {}
