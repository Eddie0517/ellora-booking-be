package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.ServiceCategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity, Long> {
    List<ServiceCategoryEntity> findByActiveTrueOrderByNameAsc();
    List<ServiceCategoryEntity> findAllByOrderByNameAsc();
}
