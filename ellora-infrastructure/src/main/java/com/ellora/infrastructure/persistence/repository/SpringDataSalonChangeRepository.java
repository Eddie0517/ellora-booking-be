package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.SalonChangeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSalonChangeRepository extends JpaRepository<SalonChangeEntity, Long> {
    Page<SalonChangeEntity> findByStatusOrderBySubmittedAtDesc(String status, Pageable pageable);
    long countByStatus(String status);
}
