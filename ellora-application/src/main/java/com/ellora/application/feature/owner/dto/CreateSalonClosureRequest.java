package com.ellora.application.feature.owner.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateSalonClosureRequest(
        @NotNull LocalDate date,
        @Size(max = 500) String reason) { }
