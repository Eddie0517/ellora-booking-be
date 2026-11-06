package com.ellora.api.controller;

import com.ellora.application.feature.billing.ManageBillingQrUseCase;
import com.ellora.application.feature.billing.dto.BillingQrResponse;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/billing/qr")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Billing", description = "Payment QR configuration for salon subscription requests")
public class AdminBillingController {
    private final ManageBillingQrUseCase billingQr;

    @GetMapping
    @Operation(summary = "Get the configured platform payment QR")
    public ResponseEntity<BillingQrResponse> getQr() {
        return ResponseEntity.ok(billingQr.get());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
}
