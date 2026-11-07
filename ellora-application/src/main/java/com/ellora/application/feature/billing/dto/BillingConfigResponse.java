package com.ellora.application.feature.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillingConfigResponse(
        BigDecimal currentPercent,
        BigDecimal nextPercent,
        LocalDate effectiveMonth,
        String qrUrl
) {}
