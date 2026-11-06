package com.ellora.application.feature.salon;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.WorkingHour;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonClosureRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.WorkingHoursRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalonAvailabilityUseCase {
    private static final ZoneId SALON_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int SLOT_INTERVAL_MINUTES = 30;

    private final SalonRepository salons;
    private final NailServiceRepository services;
    private final EmployeeRepository employees;
    private final WorkingHoursRepository workingHours;
    private final SalonClosureRepository closures;
    private final BookingRepository bookings;

    @Transactional(readOnly = true)
    public List<LocalTime> execute(Long salonId, LocalDate date, Long serviceId, Long employeeId) {
        NailService service = validateRequest(salonId, date, serviceId, employeeId);
        List<WorkingHour> weeklySchedule = workingHours.findBySalonId(salonId);
        if (weeklySchedule.isEmpty() || closures.findBySalonIdAndDate(salonId, date).isPresent()) return List.of();
        WorkingHour day = weeklySchedule.stream().filter(hour -> hour.dayOfWeek() == date.getDayOfWeek())
                .findFirst().orElse(null);
        if (day == null || day.closed() || day.openTime() == null || day.closeTime() == null) return List.of();

        List<Employee> candidates = candidateEmployees(salonId, employeeId);
        LocalDate tomorrow = date.plusDays(1);
        Instant dayStart = date.atStartOfDay(SALON_ZONE).toInstant();
        Instant dayEnd = tomorrow.atStartOfDay(SALON_ZONE).toInstant();
        List<Booking> dayBookings = bookings.findBySalonIdAndScheduledAtBetween(salonId, dayStart, dayEnd).stream()
                .filter(this::occupiesTime).toList();

        LocalTime first = day.openTime();
        if (date.equals(LocalDate.now(SALON_ZONE))) {
            Instant earliest = Instant.now().plus(Duration.ofHours(1));
            while (date.atTime(first).atZone(SALON_ZONE).toInstant().isBefore(earliest)) {
                first = first.plusMinutes(SLOT_INTERVAL_MINUTES);
            }
        }
        LocalTime lastStart = day.closeTime().minusMinutes(service.getDurationMinutes());
        var result = new java.util.ArrayList<LocalTime>();
        for (LocalTime slot = first; !slot.isAfter(lastStart); slot = slot.plusMinutes(SLOT_INTERVAL_MINUTES)) {
            Instant start = date.atTime(slot).atZone(SALON_ZONE).toInstant();
            Instant end = start.plus(Duration.ofMinutes(service.getDurationMinutes()));
            boolean free = candidates.stream().anyMatch(employee -> dayBookings.stream()
                    .noneMatch(booking -> conflicts(booking, employee == null ? null : employee.getId(), start, end)));
            if (free) result.add(slot);
        }
        return List.copyOf(result);
    }

    @Transactional(readOnly = true)
    public void assertAvailable(Long salonId, LocalDate date, Long serviceId, Long employeeId, Instant scheduledAt) {
        NailService service = validateRequest(salonId, date, serviceId, employeeId);
        if (closures.findBySalonIdAndDate(salonId, date).isPresent()) {
            throw new BusinessRuleException("SALON_CLOSED", "Salon đóng cửa vào ngày đã chọn");
        }
        List<WorkingHour> schedule = workingHours.findBySalonId(salonId);
        if (schedule.isEmpty()) return; // Keep bookings compatible for salons that have not configured hours yet.
        List<LocalTime> available = execute(salonId, date, serviceId, employeeId);
        LocalTime requested = scheduledAt.atZone(SALON_ZONE).toLocalTime();
        if (requested.getSecond() != 0 || requested.getNano() != 0) {
            throw new BusinessRuleException("SLOT_UNAVAILABLE", "Khung giờ phải bắt đầu đúng phút");
        }
        if (!available.contains(requested)) {
            throw new BusinessRuleException("SLOT_UNAVAILABLE", "Khung giờ này không còn trống hoặc nằm ngoài giờ hoạt động");
        }
    }

    private NailService validateRequest(Long salonId, LocalDate date, Long serviceId, Long employeeId) {
        if (date == null || date.isBefore(LocalDate.now(SALON_ZONE))) {
            throw new BusinessRuleException("INVALID_DATE_RANGE", "Ngày đặt lịch không hợp lệ");
        }
        if (salons.findById(salonId).isEmpty()) throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon");
        NailService service = services.findById(serviceId)
                .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
        if (!salonId.equals(service.getSalonId())) {
            throw new BusinessRuleException("SERVICE_NOT_IN_SALON", "Dịch vụ không thuộc salon đã chọn");
        }
        if (employeeId != null) {
            Employee employee = employees.findById(employeeId)
                    .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
            if (!salonId.equals(employee.getSalonId()) || !employee.isActive()) {
                throw new BusinessRuleException("EMPLOYEE_NOT_IN_SALON", "Nhân viên không thuộc salon hoặc đã ngừng hoạt động");
            }
        }
        return service;
    }

    private List<Employee> candidateEmployees(Long salonId, Long employeeId) {
        if (employeeId != null) return employees.findById(employeeId).stream().toList();
        List<Employee> active = employees.findBySalonIdAndActiveTrue(salonId);
        return active.isEmpty() ? java.util.Collections.singletonList(null) : active;
    }

    private boolean occupiesTime(Booking booking) {
        return booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED;
    }

    private boolean conflicts(Booking booking, Long employeeId, Instant start, Instant end) {
        if (employeeId != null && booking.getEmployeeId() != null && !employeeId.equals(booking.getEmployeeId())) return false;
        Instant bookingEnd = booking.getScheduledAt().plus(Duration.ofMinutes(booking.getDurationMinutes()));
        return booking.getScheduledAt().isBefore(end) && bookingEnd.isAfter(start);
    }
}
