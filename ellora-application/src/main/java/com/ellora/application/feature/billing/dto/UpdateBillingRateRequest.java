package com.ellora.application.feature.billing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateBillingRateRequest(
        @NotNull(message = "Tỷ lệ phí không được để trống")
        @DecimalMin(value = "0.0", message = "Tỷ lệ phí tối thiểu 0%")
        @DecimalMax(value = "100.0", message = "Tỷ lệ phí tối đa 100%")
        BigDecimal percent
) {}
