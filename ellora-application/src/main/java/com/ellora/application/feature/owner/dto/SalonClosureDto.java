package com.ellora.application.feature.owner.dto;

import java.time.LocalDate;

public record SalonClosureDto(Long id, LocalDate date, String reason) { }
