package com.ellora.api.controller;

import com.ellora.application.feature.support.GetSupportContactUseCase;
import com.ellora.application.feature.support.UpdateSupportContactUseCase;
import com.ellora.application.feature.support.dto.SupportContactResponse;
import com.ellora.application.feature.support.dto.UpdateSupportContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Support Contact", description = "Endpoints for platform support contact information")
public class SupportContactController {

    private final GetSupportContactUseCase getSupportContactUseCase;
    private final UpdateSupportContactUseCase updateSupportContactUseCase;

    @GetMapping("/api/v1/support/contact")
    @Operation(summary = "Get platform support contacts", description = "Public endpoint returning official support email, phone, and social channels")
    public ResponseEntity<SupportContactResponse> getSupportContact() {
        return ResponseEntity.ok(getSupportContactUseCase.execute());
    }

    @PutMapping("/api/v1/admin/support/contact")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update platform support contacts", description = "Admin endpoint to update official platform contact information")
    public ResponseEntity<SupportContactResponse> updateSupportContact(
            @Valid @RequestBody UpdateSupportContactRequest request) {
        return ResponseEntity.ok(updateSupportContactUseCase.execute(request));
    }
}
