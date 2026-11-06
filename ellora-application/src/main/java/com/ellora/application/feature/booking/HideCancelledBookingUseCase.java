package com.ellora.application.feature.booking;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.repository.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HideCancelledBookingUseCase {
    private final BookingRepository bookings;

    @Transactional
    public void execute(Long bookingId, UUID customerId) {
        if (customerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }
        var booking = bookings.findById(bookingId)
                .orElseThrow(() -> new BusinessRuleException("BOOKING_NOT_FOUND", "Không tìm thấy lịch hẹn"));
        if (!customerId.equals(booking.getCustomerId())) {
            throw new BusinessRuleException("FORBIDDEN", "Lịch hẹn không thuộc tài khoản của bạn");
        }
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION", "Chỉ có thể xóa lịch hẹn đã hủy khỏi danh sách");
        }
        bookings.hideCancelledForCustomer(bookingId, customerId);
    }
}
