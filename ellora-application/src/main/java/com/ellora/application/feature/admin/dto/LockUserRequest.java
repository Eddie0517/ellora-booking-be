package com.ellora.application.feature.admin.dto;

import jakarta.validation.constraints.NotNull;

public record LockUserRequest(
        @NotNull(message = "Trạng thái khóa không được để trống")
        Boolean locked
) {
}
