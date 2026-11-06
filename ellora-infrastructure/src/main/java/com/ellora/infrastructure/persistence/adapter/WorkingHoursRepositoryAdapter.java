package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.WorkingHour;
import com.ellora.domain.repository.WorkingHoursRepository;
import com.ellora.infrastructure.persistence.entity.SalonWorkingHourEntity;
import com.ellora.infrastructure.persistence.repository.SpringDataWorkingHoursRepository;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingHoursRepositoryAdapter implements WorkingHoursRepository {
    private final SpringDataWorkingHoursRepository repository;

    @Override
    public List<WorkingHour> findBySalonId(Long salonId) {
        return repository.findBySalonId(salonId).stream().map(e -> new WorkingHour(
                DayOfWeek.valueOf(e.getDayOfWeek()), e.getOpenTime(), e.getCloseTime(), e.isClosed())).toList();
    }

    @Override
    public void replace(Long salonId, List<WorkingHour> hours) {
        repository.deleteBySalonId(salonId);
        repository.flush();
        repository.saveAll(hours.stream().map(h -> {
            var e = new SalonWorkingHourEntity();
            e.setSalonId(salonId); e.setDayOfWeek(h.dayOfWeek().name()); e.setOpenTime(h.openTime());
            e.setCloseTime(h.closeTime()); e.setClosed(h.closed());
            return e;
        }).toList());
    }
}
