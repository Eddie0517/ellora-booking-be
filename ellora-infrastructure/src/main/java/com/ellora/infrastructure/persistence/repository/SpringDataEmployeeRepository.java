package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.EmployeeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    List<EmployeeEntity> findBySalonIdAndActiveTrue(Long salonId);

    List<EmployeeEntity> findBySalonIdOrderByIdAsc(Long salonId);
}
