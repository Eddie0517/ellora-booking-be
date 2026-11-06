package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingHourDto(@NotNull DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, @NotNull Boolean closed) {}
