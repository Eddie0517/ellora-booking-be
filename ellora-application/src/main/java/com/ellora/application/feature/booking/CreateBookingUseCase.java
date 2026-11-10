package com.ellora.application.feature.booking;

import com.ellora.application.feature.booking.dto.CreateBookingRequest;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.salon.GetSalonAvailabilityUseCase;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonRepository;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final SalonRepository salonRepository;
    private final NailServiceRepository nailServiceRepository;
    private final EmployeeRepository employeeRepository;
    private final GetSalonAvailabilityUseCase availability;
    private final BookingResponseMapper responseMapper;

    @Transactional
    public BookingDetailsResponse execute(UUID customerId, CreateBookingRequest request) {
        Salon salon = salonRepository.findById(request.salonId())
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND",
                        "Không tìm thấy salon với mã định danh " + request.salonId()));

        if (salon.isOverdue()) {
            throw new BusinessRuleException("SALON_PAYMENT_OVERDUE",
                    "Tiệm tạm ngưng nhận lịch hẹn do chưa hoàn tất nghĩa vụ phí nền tảng");
        }

        NailService service = nailServiceRepository.findById(request.serviceId())
                .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND",
                        "Không tìm thấy dịch vụ với mã " + request.serviceId()));

        if (!salon.getId().equals(service.getSalonId())) {
            throw new BusinessRuleException("SERVICE_NOT_IN_SALON",
                    "Dịch vụ không thuộc về salon đã chọn");
        }

        if (request.employeeId() != null) {
            Employee employee = employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND",
                            "Không tìm thấy nhân viên kỹ thuật với mã " + request.employeeId()));
            if (!salon.getId().equals(employee.getSalonId())) {
                throw new BusinessRuleException("EMPLOYEE_NOT_IN_SALON",
                        "Nhân viên kỹ thuật không thuộc về salon đã chọn");
            }
        }

        int duration = (request.durationMinutes() != null && request.durationMinutes() > 0)
                ? request.durationMinutes()
                : service.getDurationMinutes();

        availability.assertAvailable(salon.getId(), request.scheduledAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                service.getId(), request.employeeId(), request.scheduledAt());

        BigDecimal priceSnapshot = service.getPrice();

        Booking booking;
        if (customerId != null) {
            booking = Booking.createNew(
                    customerId,
                    salon.getId(),
                    service.getId(),
                    request.employeeId(),
                    request.scheduledAt(),
                    duration,
                    priceSnapshot,
                    request.customerNote()
            );
            if (request.customerPhone() != null && !request.customerPhone().isBlank()) {
                booking.setCustomerPhone(request.customerPhone().trim());
            }
        } else {
            booking = Booking.createGuest(
                    request.walkInName(),
                    request.guestEmail(),
                    request.walkInPhone(),
                    salon.getId(),
                    service.getId(),
                    request.employeeId(),
                    request.scheduledAt(),
                    duration,
                    priceSnapshot,
                    request.customerNote()
            );
        }

        Booking saved = bookingRepository.save(booking);

        return responseMapper.toResponse(saved);
    }
}
