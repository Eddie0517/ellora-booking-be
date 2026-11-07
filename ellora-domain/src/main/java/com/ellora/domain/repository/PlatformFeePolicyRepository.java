package com.ellora.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface PlatformFeePolicyRepository {
    Optional<BigDecimal> findRateForMonth(LocalDate month);
    void saveRate(LocalDate effectiveMonth, BigDecimal percent);
}
