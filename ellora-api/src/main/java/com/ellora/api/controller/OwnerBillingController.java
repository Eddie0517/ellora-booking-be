package com.ellora.api.controller;

import com.ellora.application.feature.billing.PlatformBillingUseCase;
import com.ellora.application.feature.billing.dto.PlatformBillResponse;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner/billing")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Owner Billing", description = "Endpoints for salon owners to view monthly platform bills, fees, and PayOS payment details")
public class OwnerBillingController {

    private final PlatformBillingUseCase platformBillingUseCase;

    private UUID getOwnerId(Object principal) {
        if (principal instanceof UUID id) return id;
        throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập với quyền chủ salon");
    }

    @GetMapping
    @Operation(summary = "Get owner salon monthly platform fee statement and payment info")
    public ResponseEntity<PlatformBillResponse> getOwnerBill(
            @RequestParam String month,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(platformBillingUseCase.getOwnerBill(getOwnerId(principal), month));
    }

    @GetMapping("/status")
    @Operation(summary = "Check if owner salon is locked due to overdue platform fees")
    public ResponseEntity<Map<String, Object>> getBillingStatus(@AuthenticationPrincipal Object principal) {
        boolean overdue = platformBillingUseCase.checkOwnerOverdue(getOwnerId(principal));
        return ResponseEntity.ok(Map.of("isOverdueLocked", overdue));
    }

    @PostMapping("/prepare-payment")
    @Operation(summary = "Prepare a PayOS payment link and VietQR code for the specified month")
    public ResponseEntity<PlatformBillResponse> preparePayment(
            @RequestParam String month,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(platformBillingUseCase.prepareBillingPayment(getOwnerId(principal), month));
    }

    @PostMapping("/sync-payment")
    @Operation(summary = "Sync PayOS payment status and settle overdue platform fees")
    public ResponseEntity<PlatformBillResponse> syncPayment(
            @RequestParam String month,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(platformBillingUseCase.syncBillingPayment(getOwnerId(principal), month));
    }
}
