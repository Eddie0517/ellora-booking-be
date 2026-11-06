package com.ellora.api.controller;

import com.ellora.application.feature.booking.CreateBookingUseCase;
import com.ellora.application.feature.booking.GetBookingDetailsUseCase;
import com.ellora.application.feature.booking.GetMyBookingsUseCase;
import com.ellora.application.feature.booking.BookingAccessUseCase;
import com.ellora.application.feature.booking.HideCancelledBookingUseCase;
import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.booking.TransitionBookingStatusUseCase;
import com.ellora.application.feature.booking.RespondToCompletionUseCase;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.CreateBookingRequest;
import com.ellora.application.feature.booking.dto.TransitionStatusRequest;
import com.ellora.application.feature.booking.dto.CompletionResponseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints for scheduling appointments, retrieving booking details, and updating status")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final GetBookingDetailsUseCase getBookingDetailsUseCase;
    private final GetMyBookingsUseCase getMyBookingsUseCase;
    private final BookingAccessUseCase bookingAccessUseCase;
    private final TransitionBookingStatusUseCase transitionBookingStatusUseCase;
    private final RespondToCompletionUseCase respondToCompletionUseCase;
    private final HideCancelledBookingUseCase hideCancelledBookingUseCase;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create an appointment booking",
            description = "Creates an appointment for the authenticated customer; owner walk-ins use the owner booking endpoint"
    )
    public ResponseEntity<BookingDetailsResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UUID customerId) {
        BookingDetailsResponse response = createBookingUseCase.execute(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedResult<BookingDetailsResponse>> getMyBookings(
            @AuthenticationPrincipal UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(getMyBookingsUseCase.execute(customerId, page, pageSize));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove a cancelled booking from the customer's list")
    @ApiResponse(responseCode = "204", description = "Cancelled booking removed from the customer's list")
    public ResponseEntity<Void> hideCancelledBooking(@PathVariable Long id,
            @AuthenticationPrincipal UUID customerId) {
        hideCancelledBookingUseCase.execute(id, customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get booking details by ID",
            description = "Retrieves complete booking details and scheduling status"
    )
    public ResponseEntity<BookingDetailsResponse> getBookingDetails(@PathVariable Long id, Authentication authentication) {
        bookingAccessUseCase.check(id, userId(authentication), isAdmin(authentication), null);
        BookingDetailsResponse response = getBookingDetailsUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update booking status",
            description = "Transitions booking status (CONFIRMED, COMPLETED, CANCELLED, DISPUTED)"
    )
    public ResponseEntity<BookingDetailsResponse> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransitionStatusRequest request,
            Authentication authentication) {
        bookingAccessUseCase.check(id, userId(authentication), isAdmin(authentication), request.status());
        BookingDetailsResponse response = transitionBookingStatusUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/completion-response")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Customer confirms or disputes a completed booking")
    public ResponseEntity<BookingDetailsResponse> respondToCompletion(
            @PathVariable Long id, @Valid @RequestBody CompletionResponseRequest request,
            @AuthenticationPrincipal UUID customerId) {
        return ResponseEntity.ok(respondToCompletionUseCase.execute(id, customerId, request));
    }

    private UUID userId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof UUID id ? id : null;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
