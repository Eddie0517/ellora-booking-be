package com.ellora.api.controller;

import com.ellora.application.feature.booking.CreateBookingUseCase;
import com.ellora.application.feature.booking.GetBookingDetailsUseCase;
import com.ellora.application.feature.booking.TransitionBookingStatusUseCase;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.CreateBookingRequest;
import com.ellora.application.feature.booking.dto.TransitionStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints for scheduling appointments, retrieving booking details, and updating status")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final GetBookingDetailsUseCase getBookingDetailsUseCase;
    private final TransitionBookingStatusUseCase transitionBookingStatusUseCase;

    @PostMapping
    @Operation(
            summary = "Create an appointment booking",
            description = "Creates a booking for an authenticated user or a guest (walk-in) customer"
    )
    public ResponseEntity<BookingDetailsResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal Object principal) {
        UUID customerId = (principal instanceof UUID uuid) ? uuid : null;
        BookingDetailsResponse response = createBookingUseCase.execute(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get booking details by ID",
            description = "Retrieves complete booking details and scheduling status"
    )
    public ResponseEntity<BookingDetailsResponse> getBookingDetails(@PathVariable Long id) {
        BookingDetailsResponse response = getBookingDetailsUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update booking status",
            description = "Transitions booking status (CONFIRMED, COMPLETED, CANCELLED, DISPUTED)"
    )
    public ResponseEntity<BookingDetailsResponse> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransitionStatusRequest request) {
        BookingDetailsResponse response = transitionBookingStatusUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }
}
