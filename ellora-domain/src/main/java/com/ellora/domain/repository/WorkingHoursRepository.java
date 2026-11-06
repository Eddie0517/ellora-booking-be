package com.ellora.domain.repository;

import com.ellora.domain.model.WorkingHour;
import java.util.List;

public interface WorkingHoursRepository {
    List<WorkingHour> findBySalonId(Long salonId);
    void replace(Long salonId, List<WorkingHour> hours);
}
