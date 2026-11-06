package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.ManageBookingDisputesUseCase;
import com.ellora.application.feature.admin.dto.BookingDisputeResponse;
import com.ellora.application.feature.admin.dto.ResolveBookingDisputeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/booking-disputes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingDisputeController {
    private final ManageBookingDisputesUseCase useCase;

    @GetMapping
    @Operation(summary = "List customer booking disputes")
    public ResponseEntity<PagedResult<BookingDisputeResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(useCase.list(status, page, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking dispute details")
    public ResponseEntity<BookingDisputeResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.detail(id));
    }

    @PatchMapping("/{id}/resolution")
    @Operation(summary = "Resolve a booking dispute")
    public ResponseEntity<BookingDisputeResponse> resolve(
            @PathVariable Long id, @Valid @RequestBody ResolveBookingDisputeRequest request) {
        return ResponseEntity.ok(useCase.resolve(id, request));
    }
}
