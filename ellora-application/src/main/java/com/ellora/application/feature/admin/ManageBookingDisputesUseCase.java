package com.ellora.application.feature.admin;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.dto.BookingDisputeResponse;
import com.ellora.application.feature.admin.dto.ResolveBookingDisputeRequest;
import com.ellora.application.feature.booking.BookingResponseMapper;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageBookingDisputesUseCase {
    private final BookingRepository bookings;
    private final BookingResponseMapper bookingMapper;
    private final SalonRepository salons;
    private final UserRepository users;
    private final EmployeeRepository employees;

    @Transactional(readOnly = true)
    public PagedResult<BookingDisputeResponse> list(String status, int page, int pageSize) {
        String filter = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        if (filter != null && !Set.of("OPEN", "UPHELD", "REOPENED").contains(filter)) {
            throw new BusinessRuleException("INVALID_DISPUTE_STATUS", "Trạng thái phản ánh không hợp lệ");
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, pageSize));
        var items = bookings.findDisputes(filter, safePage, safeSize).stream().map(this::toResponse).toList();
        return PagedResult.of(items, safePage, safeSize, bookings.countDisputes(filter));
    }

    @Transactional(readOnly = true)
    public BookingDisputeResponse detail(Long id) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new BusinessRuleException("DISPUTE_NOT_FOUND", "Không tìm thấy phản ánh"));
        if (booking.getCompletionCustomerNote() == null) {
            throw new BusinessRuleException("DISPUTE_NOT_FOUND", "Không tìm thấy phản ánh");
        }
        return toResponse(booking);
    }

    @Transactional
    public BookingDisputeResponse resolve(Long id, ResolveBookingDisputeRequest request) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new BusinessRuleException("DISPUTE_NOT_FOUND", "Không tìm thấy phản ánh"));
        booking.resolveDispute(request.outcome(), request.note());
        return toResponse(bookings.save(booking));
    }

    private BookingDisputeResponse toResponse(Booking booking) {
        var basic = bookingMapper.toResponse(booking);
        User customer = booking.getCustomerId() == null ? null : users.findById(booking.getCustomerId()).orElse(null);
        Salon salon = salons.findById(booking.getSalonId()).orElse(null);
        User owner = salon == null ? null : users.findById(salon.getOwnerId()).orElse(null);
        String employeeName = booking.getEmployeeId() == null ? null
                : employees.findById(booking.getEmployeeId()).map(Employee::getFullName).orElse(null);
        String status = booking.getCompletionResolvedAt() == null ? "OPEN" : booking.getCompletionResolution();
        return new BookingDisputeResponse(
                booking.getId(), booking.getId(), booking.getCompletionVersion(), status,
                booking.getCompletionCustomerNote(), booking.getCompletionRespondedAt(),
                booking.getCompletionResolvedAt(), booking.getCompletionAdminNote(),
                basic.customerName(), basic.customerPhone(), customer == null ? booking.getGuestEmail() : customer.getEmail(),
                booking.getSalonId(), basic.salonName(), salon == null ? null : salon.getPhone(),
                salon == null ? null : salon.getEmail(), salon == null ? null : salon.getAddress(),
                owner == null ? null : owner.getFullName(), owner == null ? null : owner.getPhone(),
                owner == null ? null : owner.getEmail(), basic.serviceName(), booking.getServicePriceSnapshot(),
                booking.getScheduledAt(), booking.getDurationMinutes(), employeeName,
                booking.getStatus() == null ? null : booking.getStatus().name(), booking.getCompletedAt());
    }
}
