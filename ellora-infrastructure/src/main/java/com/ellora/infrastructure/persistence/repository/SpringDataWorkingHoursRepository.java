package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.SalonWorkingHourEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWorkingHoursRepository extends JpaRepository<SalonWorkingHourEntity, Long> {
    List<SalonWorkingHourEntity> findBySalonId(Long salonId);
    void deleteBySalonId(Long salonId);
}
