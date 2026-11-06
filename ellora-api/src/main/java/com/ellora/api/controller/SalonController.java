package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.salon.CreateSalonUseCase;
import com.ellora.application.feature.salon.GetSalonDetailsUseCase;
import com.ellora.application.feature.salon.GetSalonServicesUseCase;
import com.ellora.application.feature.salon.GetSalonStaffUseCase;
import com.ellora.application.feature.salon.GetSalonAvailabilityUseCase;
import com.ellora.application.feature.salon.GetSalonsUseCase;
import com.ellora.application.feature.owner.WorkingHoursUseCase;
import com.ellora.application.feature.owner.SalonClosureUseCase;
import com.ellora.application.feature.owner.dto.WorkingHourDto;
import com.ellora.application.feature.salon.dto.CreateSalonRequest;
import com.ellora.application.feature.salon.dto.EmployeeResponse;
import com.ellora.application.feature.salon.dto.NailServiceResponse;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.application.feature.salon.dto.SalonSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/salons")
@RequiredArgsConstructor
@Tag(name = "Salons", description = "Endpoints for discovering salons, viewing service menus and staff members")
public class SalonController {

    private final GetSalonsUseCase getSalonsUseCase;
    private final GetSalonDetailsUseCase getSalonDetailsUseCase;
    private final GetSalonServicesUseCase getSalonServicesUseCase;
    private final GetSalonStaffUseCase getSalonStaffUseCase;
    private final CreateSalonUseCase createSalonUseCase;
    private final WorkingHoursUseCase workingHoursUseCase;
    private final SalonClosureUseCase salonClosureUseCase;
    private final GetSalonAvailabilityUseCase getSalonAvailabilityUseCase;

    @GetMapping
    @Operation(summary = "Search and list salons", description = "Returns paginated list of active nail salons matching city and keyword filters")
    public ResponseEntity<PagedResult<SalonSummaryResponse>> getSalons(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PagedResult<SalonSummaryResponse> result = getSalonsUseCase.execute(city, keyword, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get salon details", description = "Returns full profile, address, and ratings for a specific salon")
    public ResponseEntity<SalonDetailsResponse> getSalonDetails(@PathVariable Long id) {
        SalonDetailsResponse response = getSalonDetailsUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/working-hours")
    public ResponseEntity<List<WorkingHourDto>> getWorkingHours(@PathVariable Long id) {
        return ResponseEntity.ok(workingHoursUseCase.forSalon(id));
    }

    @GetMapping("/{id}/closures")
    public ResponseEntity<List<LocalDate>> getClosures(@PathVariable Long id,
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ResponseEntity.ok(salonClosureUseCase.datesForSalon(id, from, to));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get available booking times", description = "Returns 30-minute start times that fit salon hours, closures, the service duration, and existing bookings")
    public ResponseEntity<List<LocalTime>> getAvailability(@PathVariable Long id,
            @RequestParam LocalDate date, @RequestParam Long serviceId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(getSalonAvailabilityUseCase.execute(id, date, serviceId, employeeId));
    }

    @GetMapping("/{id}/services")
    @Operation(summary = "Get salon services", description = "Returns service menu and pricing for the specified salon")
    public ResponseEntity<List<NailServiceResponse>> getSalonServices(@PathVariable Long id) {
        List<NailServiceResponse> services = getSalonServicesUseCase.execute(id);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}/staff")
    @Operation(summary = "Get salon staff", description = "Returns active staff members working at the specified salon")
    public ResponseEntity<List<EmployeeResponse>> getSalonStaff(@PathVariable Long id) {
        List<EmployeeResponse> staff = getSalonStaffUseCase.execute(id);
        return ResponseEntity.ok(staff);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Register new salon", description = "Enables salon owners to register a new salon profile pending approval")
    public ResponseEntity<SalonDetailsResponse> createSalon(
            @AuthenticationPrincipal UUID ownerId,
            @Valid @RequestBody CreateSalonRequest request) {
        SalonDetailsResponse response = createSalonUseCase.execute(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
