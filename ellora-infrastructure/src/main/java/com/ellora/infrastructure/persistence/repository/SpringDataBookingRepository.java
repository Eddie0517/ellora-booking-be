package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.BookingEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataBookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByCustomerIdAndCustomerHiddenAtIsNullOrderByScheduledAtDesc(UUID customerId);

    Page<BookingEntity> findByCustomerIdAndCustomerHiddenAtIsNullOrderByScheduledAtDesc(UUID customerId, Pageable pageable);

    long countByCustomerIdAndCustomerHiddenAtIsNull(UUID customerId);

    @Modifying
    @Query("UPDATE BookingEntity b SET b.customerHiddenAt = :hiddenAt " +
            "WHERE b.id = :bookingId AND b.customerId = :customerId " +
            "AND b.status = 'CANCELLED' AND b.customerHiddenAt IS NULL")
    int hideCancelledForCustomer(@Param("bookingId") Long bookingId,
            @Param("customerId") UUID customerId, @Param("hiddenAt") Instant hiddenAt);

    @Query(value = "SELECT b FROM BookingEntity b WHERE b.completionCustomerNote IS NOT NULL " +
            "AND (:status IS NULL OR (:status = 'OPEN' AND b.completionResolvedAt IS NULL) " +
            "OR b.completionResolution = :status) ORDER BY b.completionRespondedAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b WHERE b.completionCustomerNote IS NOT NULL " +
                    "AND (:status IS NULL OR (:status = 'OPEN' AND b.completionResolvedAt IS NULL) " +
                    "OR b.completionResolution = :status)")
    Page<BookingEntity> findDisputes(@Param("status") String status, Pageable pageable);

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
