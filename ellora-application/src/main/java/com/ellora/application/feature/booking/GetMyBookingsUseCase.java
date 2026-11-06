package com.ellora.application.feature.booking;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.repository.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMyBookingsUseCase {
    private final BookingRepository bookings;
    private final BookingResponseMapper responseMapper;

    @Transactional(readOnly = true)
    public PagedResult<BookingDetailsResponse> execute(UUID customerId, int page, int pageSize) {
        if (customerId == null) throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, pageSize));
        var items = bookings.findByCustomerId(customerId, safePage, safeSize).stream()
                .map(responseMapper::toResponse).toList();
        return PagedResult.of(items, safePage, safeSize, bookings.countByCustomerId(customerId));
    }
}
