package com.ellora.application.feature.booking;

import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.CompletionResponseRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RespondToCompletionUseCase {
    private final BookingRepository bookings;
    private final BookingResponseMapper responseMapper;

    @Transactional
    public BookingDetailsResponse execute(Long bookingId, UUID customerId, CompletionResponseRequest request) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new BusinessRuleException("BOOKING_NOT_FOUND", "Không tìm thấy lịch hẹn"));
        if (customerId == null || !customerId.equals(booking.getCustomerId())) {
            throw new BusinessRuleException("FORBIDDEN", "Bạn không có quyền phản hồi lịch hẹn này");
        }
        booking.respondToCompletion(request.completed(), request.completionVersion(), request.reason());
        return responseMapper.toResponse(bookings.save(booking));
    }
}
