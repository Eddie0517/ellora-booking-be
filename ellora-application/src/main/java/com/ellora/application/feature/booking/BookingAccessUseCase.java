package com.ellora.application.feature.booking;

import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.SalonRepository;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingAccessUseCase {
    private final BookingRepository bookings;
    private final SalonRepository salons;

    @Transactional(readOnly = true)
    public void check(Long bookingId, UUID userId, boolean admin, String targetStatus) {
        if (userId == null) throw new BusinessRuleException("FORBIDDEN", "Bạn không có quyền truy cập lịch hẹn");
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new BusinessRuleException("BOOKING_NOT_FOUND", "Không tìm thấy lịch hẹn"));
        if (admin) return;
        String status = targetStatus == null ? null : targetStatus.trim().toUpperCase(Locale.ROOT);
        boolean customer = userId.equals(booking.getCustomerId());
        if (customer && (status == null || "CANCELLED".equals(status))) return;
        boolean owner = salons.findById(booking.getSalonId())
                .map(salon -> userId.equals(salon.getOwnerId())).orElse(false);
        if (owner && (status == null || Set.of("CONFIRMED", "COMPLETED", "CANCELLED").contains(status))) return;
        throw new BusinessRuleException("FORBIDDEN", "Bạn không có quyền thực hiện thao tác này");
    }
}
