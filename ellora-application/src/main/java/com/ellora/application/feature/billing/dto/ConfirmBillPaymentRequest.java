package com.ellora.application.feature.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConfirmBillPaymentRequest(
        @NotNull(message = "Số tiền xác nhận không được để trống")
        @DecimalMin(value = "0.0", message = "Số tiền phải lớn hơn hoặc bằng 0")
        BigDecimal expectedDue
) {}
