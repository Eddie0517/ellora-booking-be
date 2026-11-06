package com.ellora.application.feature.booking;

import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBookingDetailsUseCase {

    private final BookingRepository bookingRepository;
    private final BookingResponseMapper responseMapper;

    @Transactional(readOnly = true)
    public BookingDetailsResponse execute(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("BOOKING_NOT_FOUND",
                        "Không tìm thấy lịch hẹn với mã định danh " + id));

        return responseMapper.toResponse(booking);
    }
}
