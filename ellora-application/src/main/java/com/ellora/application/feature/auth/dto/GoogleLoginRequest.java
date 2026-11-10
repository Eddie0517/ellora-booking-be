package com.ellora.application.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Google credential token must not be blank")
        String credential
) {}
