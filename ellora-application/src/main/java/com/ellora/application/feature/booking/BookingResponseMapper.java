package com.ellora.application.feature.booking;

import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingResponseMapper {
    private final UserRepository users;
    private final SalonRepository salons;
    private final NailServiceRepository services;

    public BookingDetailsResponse toResponse(Booking booking) {
        User customer = booking.getCustomerId() == null ? null
                : users.findById(booking.getCustomerId()).orElse(null);
        String customerName = customer == null ? booking.getWalkInName() : customer.getFullName();
        String customerPhone = booking.getCustomerId() == null ? booking.getWalkInPhone()
                : booking.getCustomerPhone() != null && !booking.getCustomerPhone().isBlank()
                        ? booking.getCustomerPhone() : customer == null ? null : customer.getPhone();
        String salonName = salons.findById(booking.getSalonId()).map(Salon::getName).orElse(null);
        String serviceName = services.findById(booking.getServiceId()).map(NailService::getName).orElse(null);
        return new BookingDetailsResponse(
                booking.getId(), booking.getCustomerId(), booking.getWalkInName(), booking.getGuestEmail(),
                booking.getWalkInPhone(), booking.getSalonId(), booking.getServiceId(), booking.getEmployeeId(),
                booking.getScheduledAt(), booking.getDurationMinutes(), booking.getServicePriceSnapshot(),
                booking.getStatus() == null ? null : booking.getStatus().name(), booking.getCustomerNote(),
                booking.getSalonNote(), booking.getCancellationReason(), booking.getCompletedAt(),
                booking.getCreatedAt(), booking.getUpdatedAt(), customerName, customerPhone, salonName, serviceName,
                booking.getCompletionConfirmation(), booking.getCompletionVersion());
    }
}
