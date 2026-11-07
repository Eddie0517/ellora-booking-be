package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.billing.ManageBillingQrUseCase;
import com.ellora.application.feature.billing.PlatformBillingUseCase;
import com.ellora.application.feature.billing.dto.BillingConfigResponse;
import com.ellora.application.feature.billing.dto.BillingQrResponse;
import com.ellora.application.feature.billing.dto.ConfirmBillPaymentRequest;
import com.ellora.application.feature.billing.dto.PlatformBillResponse;
import com.ellora.application.feature.billing.dto.UpdateBillingRateRequest;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/billing")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Billing", description = "Endpoints for platform admin to manage fee rates, payment QR, and reconcile salon statements")
public class AdminBillingController {

    private final ManageBillingQrUseCase billingQr;
    private final PlatformBillingUseCase platformBillingUseCase;

    private UUID getAdminId(Object principal) {
        if (principal instanceof UUID id) return id;
        throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập với quyền quản trị viên");
    }

    @GetMapping("/qr")
    @Operation(summary = "Get the configured platform payment QR")
    public ResponseEntity<BillingQrResponse> getQr() {
        return ResponseEntity.ok(billingQr.get());
    }

    @PostMapping(value = "/qr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and configure the platform payment QR")
    public ResponseEntity<BillingQrResponse> uploadQr(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("INVALID_FILE", "Vui lòng chọn tệp tin hình ảnh");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(billingQr.upload(
                    file.getOriginalFilename(), file.getInputStream(), file.getSize(), file.getContentType()));
        } catch (IOException e) {
            throw new BusinessRuleException("FILE_READ_ERROR", "Không thể đọc dữ liệu hình ảnh");
        }
    }

    @GetMapping("/config")
    @Operation(summary = "Get platform billing configuration and fee rates")
    public ResponseEntity<BillingConfigResponse> getConfig() {
        return ResponseEntity.ok(platformBillingUseCase.getConfiguration());
    }

    @PutMapping("/config")
    @Operation(summary = "Update platform fee percentage for next month")
    public ResponseEntity<BillingConfigResponse> updateRate(@Valid @RequestBody UpdateBillingRateRequest request) {
        return ResponseEntity.ok(platformBillingUseCase.setRate(request.percent()));
    }

    @GetMapping("/statements")
    @Operation(summary = "List salon billing statements for a specific month")
    public ResponseEntity<PagedResult<PlatformBillResponse>> getStatements(
            @RequestParam String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(platformBillingUseCase.listStatements(month, page, size));
    }

    @PostMapping("/statements/{salonId}/confirm")
    @Operation(summary = "Confirm payment received from a salon")
    public ResponseEntity<PlatformBillResponse> confirmPayment(
            @PathVariable Long salonId,
            @RequestParam String month,
            @Valid @RequestBody ConfirmBillPaymentRequest request,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(platformBillingUseCase.confirmPayment(salonId, month, request.expectedDue(), getAdminId(principal)));
    }

    @PostMapping("/statements/{salonId}/remind")
    @Operation(summary = "Send payment reminder to a salon")
    public ResponseEntity<Map<String, String>> remindPayment(
            @PathVariable Long salonId,
            @RequestParam String month) {
        platformBillingUseCase.remindPayment(salonId, month);
        return ResponseEntity.ok(Map.of("message", "Đã xếp hàng gửi email nhắc thanh toán."));
    }
}
