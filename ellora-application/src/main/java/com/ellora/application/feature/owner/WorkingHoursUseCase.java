package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.WorkingHourDto;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.WorkingHour;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.WorkingHoursRepository;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingHoursUseCase {
    private final SalonRepository salons;
    private final WorkingHoursRepository hours;

    @Transactional(readOnly = true)
    public List<WorkingHourDto> forSalon(Long id) {
        if (salons.findById(id).isEmpty()) throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon");
        var byDay = new EnumMap<DayOfWeek, WorkingHour>(DayOfWeek.class);
        hours.findBySalonId(id).forEach(hour -> byDay.put(hour.dayOfWeek(), hour));
        return Arrays.stream(DayOfWeek.values()).map(day -> {
            WorkingHour h = byDay.get(day);
            return h == null ? new WorkingHourDto(day, null, null, true)
                    : new WorkingHourDto(day, h.openTime(), h.closeTime(), h.closed());
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkingHourDto> forOwner(UUID owner) { return forSalon(ownerSalonId(owner)); }

    @Transactional
    public List<WorkingHourDto> save(UUID owner, List<WorkingHourDto> input) {
        Long salonId = ownerSalonId(owner);
        if (input == null || input.size() != 7 || input.stream().anyMatch(h -> h == null || h.dayOfWeek() == null || h.closed() == null)
                || input.stream().map(WorkingHourDto::dayOfWeek).distinct().count() != 7
                || input.stream().anyMatch(h -> !h.closed() && (h.openTime() == null || h.closeTime() == null || !h.openTime().isBefore(h.closeTime())))) {
            throw new BusinessRuleException("INVALID_WORKING_HOURS", "Giờ mở cửa phải có đủ bảy ngày và khoảng giờ hợp lệ");
        }
        var items = input.stream().map(h -> new WorkingHour(h.dayOfWeek(), h.closed() ? null : h.openTime(), h.closed() ? null : h.closeTime(), h.closed())).toList();
        hours.replace(salonId, items);
        return forSalon(salonId);
    }

    private Long ownerSalonId(UUID owner) {
        if (owner == null) throw new BusinessRuleException("FORBIDDEN", "Vui lòng đăng nhập");
        return salons.findByOwnerId(owner).stream().findFirst().orElseThrow(
                () -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon")).getId();
    }
}
