package com.ellora.api.controller;

import com.ellora.application.feature.subscription.SalonSubscriptionUseCase;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PayOsWebhookController {
    private final SalonSubscriptionUseCase subscriptions;

    @PostMapping("/api/v1/webhooks/payos")
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        subscriptions.handlePayosWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
