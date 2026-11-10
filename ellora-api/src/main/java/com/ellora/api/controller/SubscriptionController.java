package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.subscription.SalonSubscriptionUseCase;
import com.ellora.application.feature.subscription.dto.CreateSubscriptionRequest;
import com.ellora.application.feature.subscription.dto.DecideSubscriptionRequest;
import com.ellora.application.feature.subscription.dto.OwnerSubscriptionResponse;
import com.ellora.application.feature.subscription.dto.SaveSubscriptionPlanRequest;
import com.ellora.application.feature.subscription.dto.SubscriptionPlanResponse;
import com.ellora.application.feature.subscription.dto.SubscriptionRequestResponse;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Salon Subscriptions", description = "Salon plans, subscription requests, and payment reconciliation")
public class SubscriptionController {
    private final SalonSubscriptionUseCase subscriptions;

    @GetMapping("/salon-plans")
    @Operation(summary = "List available salon subscription plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> publicPlans() {
        return ResponseEntity.ok(subscriptions.publicPlans());
    }

    @GetMapping("/owner/subscription")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OwnerSubscriptionResponse> ownerSubscription(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(subscriptions.ownerSubscription(ownerId(principal)));
    }

    @GetMapping("/owner/subscription/requests")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedResult<SubscriptionRequestResponse>> ownerRequests(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(subscriptions.ownerRequests(ownerId(principal), page, size));
    }

    @PostMapping("/owner/subscription/requests")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriptionRequestResponse> createRequest(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptions.createRequest(ownerId(principal), request));
    }

    @DeleteMapping("/owner/subscription/requests/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel a pending unpaid subscription request")
    public ResponseEntity<Void> cancelRequest(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        subscriptions.cancelPendingRequest(ownerId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/owner/subscription/requests/{id}/prepare-payment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriptionRequestResponse> preparePayment(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        return ResponseEntity.ok(subscriptions.preparePayment(ownerId(principal), id));
    }

    @PostMapping("/owner/subscription/requests/{id}/sync-payment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriptionRequestResponse> syncOwnerPayment(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        return ResponseEntity.ok(subscriptions.syncOwnerPayment(ownerId(principal), id));
    }

    @PostMapping("/admin/subscription-requests/{id}/sync-payment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriptionRequestResponse> syncAdminPayment(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptions.syncAdminPayment(id));
    }

    @GetMapping("/admin/subscription-plans")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<SubscriptionPlanResponse>> adminPlans() {
        return ResponseEntity.ok(subscriptions.adminPlans());
    }

    @PutMapping("/admin/subscription-plans/{tier}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriptionPlanResponse> savePlan(
            @AuthenticationPrincipal Object principal,
            @PathVariable String tier,
            @Valid @RequestBody SaveSubscriptionPlanRequest request) {
        return ResponseEntity.ok(subscriptions.savePlan(ownerId(principal), tier, request));
    }

    @GetMapping("/admin/subscription-requests")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedResult<SubscriptionRequestResponse>> adminRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(subscriptions.adminRequests(status, page, size));
    }

    @PatchMapping("/admin/subscription-requests/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriptionRequestResponse> decide(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            @Valid @RequestBody DecideSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptions.decide(ownerId(principal), id, request));
    }

    private UUID ownerId(Object principal) {
        if (principal instanceof UUID id) return id;
        throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
    }
}
