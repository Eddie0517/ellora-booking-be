package com.ellora.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingHour(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, boolean closed) {}
