package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.BookingEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataBookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByCustomerIdOrderByScheduledAtDesc(UUID customerId);

    List<BookingEntity> findBySalonIdOrderByScheduledAtAsc(Long salonId);

    List<BookingEntity> findBySalonIdAndStatusOrderByScheduledAtAsc(Long salonId, String status);

    List<BookingEntity> findBySalonIdAndScheduledAtBetweenOrderByScheduledAtAsc(Long salonId, Instant start, Instant end);

    List<BookingEntity> findByWalkInPhoneOrderByScheduledAtDesc(String phone);

    long countBySalonId(Long salonId);

    long countBySalonIdAndStatus(Long salonId, String status);

    @Query("SELECT COALESCE(SUM(b.servicePriceSnapshot), 0) FROM BookingEntity b " +
           "WHERE b.salonId = :salonId AND b.status = 'COMPLETED' " +
           "AND b.completedAt >= :start AND b.completedAt < :end")
    BigDecimal sumCompletedGrossBySalonIdAndPeriod(
            @Param("salonId") Long salonId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("SELECT COUNT(DISTINCT b.customerId) FROM BookingEntity b " +
           "WHERE b.salonId = :salonId AND b.status = 'COMPLETED' " +
           "AND b.completedAt >= :start AND b.completedAt < :end AND b.customerId IS NOT NULL")
    Long countDistinctCompletedCustomersBySalonIdAndPeriod(
            @Param("salonId") Long salonId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
