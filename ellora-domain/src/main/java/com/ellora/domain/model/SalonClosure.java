package com.ellora.domain.model;

import java.time.LocalDate;

public record SalonClosure(Long id, Long salonId, LocalDate date, String reason) { }
