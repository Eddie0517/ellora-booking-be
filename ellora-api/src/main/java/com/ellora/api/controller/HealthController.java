package com.ellora.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * System health and liveness controller.
 * Exposes GET /healthz for orchestrator probes, container healthchecks, and monitoring.
 */
@RestController
@Tag(name = "System", description = "System liveness and diagnostics endpoints")
public class HealthController {

    @Schema(description = "System liveness health status")
    public record HealthStatus(
            @Schema(example = "UP", requiredMode = Schema.RequiredMode.REQUIRED) String status,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant timestamp) {}

    @GetMapping("/healthz")
    @Operation(
            summary = "Liveness check",
            description = "Returns system liveness status and current UTC timestamp",
            operationId = "healthz",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "System is healthy",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = HealthStatus.class)))
            })
    public ResponseEntity<HealthStatus> healthz() {
        return ResponseEntity.ok(new HealthStatus("UP", Instant.now()));
    }
}
