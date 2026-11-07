package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.SupportContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSupportContactRepository extends JpaRepository<SupportContactEntity, Long> {
}
