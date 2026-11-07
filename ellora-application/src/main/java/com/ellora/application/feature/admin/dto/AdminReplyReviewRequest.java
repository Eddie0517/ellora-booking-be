package com.ellora.application.feature.admin.dto;

import jakarta.validation.constraints.Size;

public record AdminReplyReviewRequest(
        @Size(max = 1000, message = "Phản hồi tối đa 1000 ký tự")
        String reply
) {}
