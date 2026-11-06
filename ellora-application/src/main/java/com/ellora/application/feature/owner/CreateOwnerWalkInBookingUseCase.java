package com.ellora.application.feature.owner;

import com.ellora.application.feature.booking.CreateBookingUseCase;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.CreateBookingRequest;
import com.ellora.application.feature.owner.dto.CreateWalkInBookingRequest;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOwnerWalkInBookingUseCase {
    private final GetOwnerSalonUseCase getOwnerSalonUseCase;
    private final CreateBookingUseCase createBookingUseCase;

    @Transactional
    public BookingDetailsResponse execute(UUID ownerId, CreateWalkInBookingRequest request) {
        SalonDetailsResponse salon = getOwnerSalonUseCase.execute(ownerId);
        CreateBookingRequest bookingRequest = new CreateBookingRequest(
                salon.id(),
                request.serviceId(),
                request.employeeId(),
                request.scheduledAt(),
                null,
                request.customerNote(),
                request.customerName(),
                null,
                request.customerPhone());
        return createBookingUseCase.execute(null, bookingRequest);
    }
}
