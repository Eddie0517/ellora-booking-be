package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.ReviewEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    Page<ReviewEntity> findBySalonIdOrderByCreatedAtDesc(Long salonId, Pageable pageable);

    long countBySalonId(Long salonId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.salonId = :salonId")
    Double calculateAverageRatingBySalonId(@Param("salonId") Long salonId);
}
