package com.ellora.application.feature.booking;

import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.TransitionStatusRequest;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransitionBookingStatusUseCase {

    private final BookingRepository bookingRepository;
    private final BookingResponseMapper responseMapper;

    @Transactional
    public BookingDetailsResponse execute(Long id, TransitionStatusRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("BOOKING_NOT_FOUND",
                        "Không tìm thấy lịch hẹn với mã định danh " + id));

        String rawStatus = request.status().trim().toUpperCase();
        BookingStatus targetStatus;
        try {
            targetStatus = BookingStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("INVALID_STATUS", "Trạng thái không hợp lệ: " + request.status());
        }

        switch (targetStatus) {
            case CONFIRMED -> booking.confirm();
            case COMPLETED -> {
                booking.setSalonNote(request.note());
                booking.complete("AWAITING_CUSTOMER");
            }
            case CANCELLED -> {
                String reason = request.cancellationReason() != null && !request.cancellationReason().isBlank()
                        ? request.cancellationReason()
                        : request.note();
                booking.cancel(reason);
            }
            case DISPUTED -> booking.dispute(request.note());
            default -> throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Không thể chuyển trạng thái trực tiếp sang " + targetStatus);
        }

        Booking updated = bookingRepository.save(booking);

        return responseMapper.toResponse(updated);
    }
}
