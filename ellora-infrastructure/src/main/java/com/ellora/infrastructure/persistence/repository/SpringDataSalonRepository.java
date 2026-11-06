package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.SalonEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSalonRepository extends JpaRepository<SalonEntity, Long> {

    List<SalonEntity> findByOwnerId(UUID ownerId);

    Page<SalonEntity> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT s FROM SalonEntity s WHERE s.status = 'ACTIVE' " +
           "AND (:city = '' OR LOWER(s.city) = LOWER(:city)) " +
           "AND (:keyword = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SalonEntity> searchActiveSalons(
            @Param("city") String city,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
