package com.ellora.api.controller;

import com.ellora.application.feature.admin.dto.CreateSalonChangeRequest;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.owner.CreateOwnerWalkInBookingUseCase;
import com.ellora.application.feature.owner.GetOwnerBookingsUseCase;
import com.ellora.application.feature.owner.GetOwnerDashboardUseCase;
import com.ellora.application.feature.owner.GetOwnerSalonUseCase;
import com.ellora.application.feature.owner.ManageOwnerEmployeesUseCase;
import com.ellora.application.feature.owner.ManageOwnerServicesUseCase;
import com.ellora.application.feature.owner.SalonClosureUseCase;
import com.ellora.application.feature.owner.SubmitSalonChangeUseCase;
import com.ellora.application.feature.owner.UpdateOwnerSalonUseCase;
import com.ellora.application.feature.owner.WorkingHoursUseCase;
import com.ellora.application.feature.owner.dto.CreateSalonClosureRequest;
import com.ellora.application.feature.owner.dto.CreateWalkInBookingRequest;
import com.ellora.application.feature.owner.dto.SalonClosureDto;
import com.ellora.application.feature.owner.dto.WorkingHourDto;
import com.ellora.application.feature.owner.dto.CreateEmployeeRequest;
import com.ellora.application.feature.owner.dto.CreateServiceRequest;
import com.ellora.application.feature.owner.dto.EmployeeResponse;
import com.ellora.application.feature.owner.dto.OwnerDashboardSummaryResponse;
import com.ellora.application.feature.owner.dto.ServiceResponse;
import com.ellora.application.feature.owner.dto.UpdateEmployeeRequest;
import com.ellora.application.feature.owner.dto.UpdateOwnerSalonRequest;
import com.ellora.application.feature.owner.dto.UpdateServiceRequest;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Owner Portal", description = "Endpoints for salon owners: manage salon profile, services, employees, and view business analytics")
public class OwnerController {

    private final GetOwnerSalonUseCase getOwnerSalonUseCase;
    private final UpdateOwnerSalonUseCase updateOwnerSalonUseCase;
    private final GetOwnerDashboardUseCase getOwnerDashboardUseCase;
    private final ManageOwnerServicesUseCase manageOwnerServicesUseCase;
    private final ManageOwnerEmployeesUseCase manageOwnerEmployeesUseCase;
    private final GetOwnerBookingsUseCase getOwnerBookingsUseCase;
    private final WorkingHoursUseCase workingHoursUseCase;
    private final SalonClosureUseCase salonClosureUseCase;
    private final CreateOwnerWalkInBookingUseCase createOwnerWalkInBookingUseCase;
    private final SubmitSalonChangeUseCase submitSalonChangeUseCase;

    private UUID getOwnerId(Object principal) {
        if (!(principal instanceof UUID ownerId)) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập với quyền chủ tiệm");
        }
        return ownerId;
    }

    @GetMapping("/salon/working-hours")
    public ResponseEntity<List<WorkingHourDto>> getWorkingHours(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(workingHoursUseCase.forOwner(getOwnerId(principal)));
    }

    @PutMapping("/salon/working-hours")
    public ResponseEntity<List<WorkingHourDto>> saveWorkingHours(@AuthenticationPrincipal Object principal,
            @Valid @RequestBody List<@Valid WorkingHourDto> request) {
        return ResponseEntity.ok(workingHoursUseCase.save(getOwnerId(principal), request));
    }

    @GetMapping("/salon/closures")
    public ResponseEntity<List<SalonClosureDto>> getClosures(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(salonClosureUseCase.forOwner(getOwnerId(principal)));
    }

    @PostMapping("/salon/closures")
    public ResponseEntity<SalonClosureDto> createClosure(@AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateSalonClosureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salonClosureUseCase.create(getOwnerId(principal), request));
    }

    @DeleteMapping("/salon/closures/{id}")
    public ResponseEntity<Void> deleteClosure(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        salonClosureUseCase.delete(getOwnerId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bookings/walk-ins")
    @Operation(summary = "Create a walk-in booking", description = "Creates a guest booking for the salon owned by the authenticated owner")
    public ResponseEntity<BookingDetailsResponse> createWalkInBooking(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateWalkInBookingRequest request) {
        BookingDetailsResponse response = createOwnerWalkInBookingUseCase.execute(getOwnerId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/salon")
    @Operation(summary = "Get owner salon profile", description = "Retrieves profile of the salon owned by current authenticated user")
    public ResponseEntity<SalonDetailsResponse> getMySalon(@AuthenticationPrincipal Object principal) {
        SalonDetailsResponse response = getOwnerSalonUseCase.execute(getOwnerId(principal));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/salon")
    @Operation(summary = "Update owner salon profile", description = "Updates details and information of current owner's salon")
    public ResponseEntity<SalonDetailsResponse> updateMySalon(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody UpdateOwnerSalonRequest request) {
        SalonDetailsResponse response = updateOwnerSalonUseCase.execute(getOwnerId(principal), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/salon/change-request")
    @Operation(summary = "Submit salon change request", description = "Submits a request to update salon information pending admin review")
    public ResponseEntity<java.util.Map<String, String>> submitChangeRequest(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateSalonChangeRequest request) {
        submitSalonChangeUseCase.execute(getOwnerId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("message", "Đã gửi yêu cầu thay đổi thông tin thành công."));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get owner dashboard analytics", description = "Retrieves aggregated statistics: total bookings, revenue, ratings, and customer metrics")
    public ResponseEntity<OwnerDashboardSummaryResponse> getDashboard(@AuthenticationPrincipal Object principal) {
        OwnerDashboardSummaryResponse response = getOwnerDashboardUseCase.execute(getOwnerId(principal));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    @Operation(summary = "Get all services in salon", description = "Retrieves all nail services offered by the owner's salon")
    public ResponseEntity<List<ServiceResponse>> getServices(@AuthenticationPrincipal Object principal) {
        List<ServiceResponse> response = manageOwnerServicesUseCase.getServices(getOwnerId(principal));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/services")
    @Operation(summary = "Add a new nail service", description = "Creates a new nail service under the owner's salon")
    public ResponseEntity<ServiceResponse> createService(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = manageOwnerServicesUseCase.createService(getOwnerId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/services/{id}")
    @Operation(summary = "Update an existing service", description = "Updates pricing, duration, or active status of a service")
    public ResponseEntity<ServiceResponse> updateService(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        ServiceResponse response = manageOwnerServicesUseCase.updateService(getOwnerId(principal), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/services/{id}")
    @Operation(summary = "Deactivate/delete a service", description = "Deactivates a service in owner's salon")
    public ResponseEntity<Void> deleteService(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id) {
        manageOwnerServicesUseCase.deleteService(getOwnerId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employees")
    @Operation(summary = "Get all employees in salon", description = "Retrieves all staff members working at owner's salon")
    public ResponseEntity<List<EmployeeResponse>> getEmployees(@AuthenticationPrincipal Object principal) {
        List<EmployeeResponse> response = manageOwnerEmployeesUseCase.getEmployees(getOwnerId(principal));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/employees")
    @Operation(summary = "Add a new employee", description = "Registers a new staff member to the owner's salon")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = manageOwnerEmployeesUseCase.createEmployee(getOwnerId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/employees/{id}")
    @Operation(summary = "Update an employee", description = "Updates details of a staff member")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse response = manageOwnerEmployeesUseCase.updateEmployee(getOwnerId(principal), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/employees/{id}")
    @Operation(summary = "Deactivate/delete an employee", description = "Deactivates a staff member in owner's salon")
    public ResponseEntity<Void> deleteEmployee(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id) {
        manageOwnerEmployeesUseCase.deleteEmployee(getOwnerId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get salon appointments", description = "Retrieves appointment bookings for the owner's salon, optionally filtered by status")
    public ResponseEntity<List<BookingDetailsResponse>> getBookings(
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) String status) {
        List<BookingDetailsResponse> response = getOwnerBookingsUseCase.execute(getOwnerId(principal), status);
        return ResponseEntity.ok(response);
    }
}
