package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.NailServiceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataNailServiceRepository extends JpaRepository<NailServiceEntity, Long> {

    List<NailServiceEntity> findBySalonIdAndActiveTrue(Long salonId);
}
