package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.SalonClosureEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSalonClosureRepository extends JpaRepository<SalonClosureEntity, Long> {
    List<SalonClosureEntity> findBySalonIdOrderByDateAsc(Long salonId);
    Optional<SalonClosureEntity> findBySalonIdAndDate(Long salonId, LocalDate date);
    Optional<SalonClosureEntity> findBySalonIdAndId(Long salonId, Long id);
}
