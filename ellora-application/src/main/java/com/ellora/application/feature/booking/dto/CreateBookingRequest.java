package com.ellora.application.feature.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

        String walkInPhone,

        @Pattern(regexp = "^[+0-9 ()-]{8,20}$", message = "Số điện thoại liên hệ không hợp lệ")
        String customerPhone
) {
    public CreateBookingRequest(Long salonId, Long serviceId, Long employeeId, Instant scheduledAt,
                                Integer durationMinutes, String customerNote, String walkInName,
                                String guestEmail, String walkInPhone) {
        this(salonId, serviceId, employeeId, scheduledAt, durationMinutes, customerNote,
                walkInName, guestEmail, walkInPhone, null);
    }
}
