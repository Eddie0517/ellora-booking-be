package com.ellora.application.feature.owner;

import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.BookingResponseMapper;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOwnerBookingsUseCase {

    private final SalonRepository salonRepository;
    private final BookingRepository bookingRepository;
    private final BookingResponseMapper responseMapper;

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> execute(UUID ownerId, String status) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }

        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }

        Long salonId = salons.get(0).getId();
        List<Booking> bookings;

        if (status != null && !status.isBlank()) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.trim().toUpperCase());
                bookings = bookingRepository.findBySalonIdAndStatus(salonId, bookingStatus);
            } catch (IllegalArgumentException e) {
                bookings = bookingRepository.findBySalonId(salonId);
            }
        } else {
            bookings = bookingRepository.findBySalonId(salonId);
        }

        return bookings.stream().map(responseMapper::toResponse).toList();
    }
}
