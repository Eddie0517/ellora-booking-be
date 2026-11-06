package com.ellora.application.feature.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String message
) {
    public static RegisterResponse of(String email) {
        return new RegisterResponse(
                null,
                email,
                "Đăng ký tài khoản thành công. Vui lòng kiểm tra hộp thư để lấy mã OTP xác thực 6 số."
        );
    }

    public static RegisterResponse of(UUID userId, String email) {
        return new RegisterResponse(
                userId,
                email,
                "Đăng ký tài khoản thành công. Vui lòng kiểm tra hộp thư để lấy mã OTP xác thực 6 số."
        );
    }
}
