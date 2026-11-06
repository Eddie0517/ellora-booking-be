package com.ellora.domain.repository;

import com.ellora.domain.model.SalonClosure;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalonClosureRepository {
    List<SalonClosure> findBySalonId(Long salonId);
    Optional<SalonClosure> findBySalonIdAndDate(Long salonId, LocalDate date);
    Optional<SalonClosure> findBySalonIdAndId(Long salonId, Long id);
    SalonClosure save(SalonClosure closure);
    void deleteById(Long id);
}
