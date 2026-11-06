package com.ellora.application.feature.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateBookingRequest(
        @NotNull(message = "Mã salon không được để trống")
        Long salonId,

        @NotNull(message = "Mã dịch vụ không được để trống")
        Long serviceId,

        Long employeeId,

        @NotNull(message = "Thời gian hẹn không được để trống")
        Instant scheduledAt,

        Integer durationMinutes,

        String customerNote,

        String walkInName,

        String guestEmail,

        String walkInPhone
) {}
