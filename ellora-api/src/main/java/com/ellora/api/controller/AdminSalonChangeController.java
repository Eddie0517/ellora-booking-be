package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.DecideSalonChangeUseCase;
import com.ellora.application.feature.admin.GetSalonChangesUseCase;
import com.ellora.application.feature.admin.dto.SalonChangeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/salon-changes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Salon Changes", description = "Endpoints for moderating salon profile update requests")
public class AdminSalonChangeController {

    private final GetSalonChangesUseCase getSalonChangesUseCase;
    private final DecideSalonChangeUseCase decideSalonChangeUseCase;

    @GetMapping
    @Operation(summary = "List pending salon profile changes", description = "Returns paginated list of submitted salon profile updates waiting for admin decision")
    public ResponseEntity<PagedResult<SalonChangeResponse>> getChanges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getSalonChangesUseCase.execute(page, size));
    }

    @PostMapping("/{id}/{action}")
    @Operation(summary = "Approve or reject salon change", description = "Applies requested salon modifications or marks them as rejected")
    public ResponseEntity<Map<String, String>> decideChange(
            @PathVariable Long id,
            @PathVariable String action,
            @AuthenticationPrincipal UUID adminId) {
        decideSalonChangeUseCase.execute(id, action, adminId);
        return ResponseEntity.ok(Map.of("message", "Đã xử lý yêu cầu thay đổi salon thành công."));
    }
}
