package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.ApproveSalonUseCase;
import com.ellora.application.feature.admin.GetAdminDashboardUseCase;
import com.ellora.application.feature.admin.GetAdminRevenueUseCase;
import com.ellora.application.feature.admin.GetAdminUsersUseCase;
import com.ellora.application.feature.admin.GetPendingSalonsUseCase;
import com.ellora.application.feature.admin.LockUserUseCase;
import com.ellora.application.feature.admin.RejectSalonUseCase;
import com.ellora.application.feature.admin.dto.AdminDashboardResponse;
import com.ellora.application.feature.admin.dto.AdminRevenueReportResponse;
import com.ellora.application.feature.admin.dto.AdminUserResponse;
import com.ellora.application.feature.admin.dto.LockUserRequest;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Portal", description = "Endpoints for platform administrators: approve salons, moderate users, and view platform metrics")
public class AdminController {

    private final GetAdminDashboardUseCase getAdminDashboardUseCase;
    private final GetAdminRevenueUseCase getAdminRevenueUseCase;
    private final GetPendingSalonsUseCase getPendingSalonsUseCase;
    private final ApproveSalonUseCase approveSalonUseCase;
    private final RejectSalonUseCase rejectSalonUseCase;
    private final GetAdminUsersUseCase getAdminUsersUseCase;
    private final LockUserUseCase lockUserUseCase;

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform metrics", description = "Retrieves aggregated counts of salons, users, and overall gross revenue")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse response = getAdminDashboardUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/revenue")
    @Operation(summary = "Get platform revenue analytics", description = "Retrieves historical gross revenue, estimated platform fees, and booking trends")
    public ResponseEntity<AdminRevenueReportResponse> getRevenue(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "month") String groupBy) {
        AdminRevenueReportResponse response = getAdminRevenueUseCase.execute(from, to, groupBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/salons/pending", "/salons"})
    @Operation(summary = "List salons for approval/history", description = "Returns paginated list of salons with optional status filter (ALL, PENDING_APPROVAL, ACTIVE, SUSPENDED, REJECTED)")
    public ResponseEntity<PagedResult<SalonDetailsResponse>> getPendingSalons(
            @RequestParam(required = false, defaultValue = "PENDING_APPROVAL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PagedResult<SalonDetailsResponse> response = getPendingSalonsUseCase.execute(status, page, pageSize);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/salons/{id}/approve", consumes = {org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE, org.springframework.http.MediaType.ALL_VALUE})
    @Operation(summary = "Approve salon", description = "Approves a pending salon with contract file and promotes owner to SALON_OWNER")
    public ResponseEntity<Map<String, String>> approveSalon(
            @PathVariable Long id,
            @RequestParam(value = "contract", required = false) org.springframework.web.multipart.MultipartFile contract) {
        if (contract != null && !contract.isEmpty()) {
            try {
                approveSalonUseCase.execute(id, contract.getOriginalFilename(), contract.getInputStream(), contract.getSize(), contract.getContentType());
            } catch (java.io.IOException e) {
                throw new BusinessRuleException("FILE_READ_ERROR", "Không thể đọc dữ liệu tệp hợp đồng");
            }
        } else {
            approveSalonUseCase.execute(id);
        }
        return ResponseEntity.ok(Map.of("message", "Đã duyệt và cấp quyền chủ salon thành công."));
    }

    @PostMapping("/salons/{id}/reject")
    @Operation(summary = "Reject/suspend salon", description = "Suspends or rejects a salon registration")
    public ResponseEntity<Map<String, String>> rejectSalon(@PathVariable Long id) {
        rejectSalonUseCase.execute(id);
        return ResponseEntity.ok(Map.of("message", "Đã từ chối/đình chỉ salon."));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Returns paginated list of users across the platform")
    public ResponseEntity<PagedResult<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PagedResult<AdminUserResponse> response = getAdminUsersUseCase.execute(page, pageSize);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/lock")
    @Operation(summary = "Lock/unlock user account", description = "Locks or unlocks a user account and revokes active sessions")
    public ResponseEntity<AdminUserResponse> lockUser(
            @PathVariable UUID id,
            @Valid @RequestBody LockUserRequest request) {
        AdminUserResponse response = lockUserUseCase.execute(id, request.locked());
        return ResponseEntity.ok(response);
    }
}
