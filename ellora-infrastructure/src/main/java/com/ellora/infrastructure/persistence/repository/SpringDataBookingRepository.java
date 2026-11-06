package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.BookingEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataBookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByCustomerIdOrderByScheduledAtDesc(UUID customerId);

    List<BookingEntity> findBySalonIdOrderByScheduledAtAsc(Long salonId);

    List<BookingEntity> findBySalonIdAndStatusOrderByScheduledAtAsc(Long salonId, String status);

    List<BookingEntity> findBySalonIdAndScheduledAtBetweenOrderByScheduledAtAsc(Long salonId, Instant start, Instant end);

    List<BookingEntity> findByWalkInPhoneOrderByScheduledAtDesc(String phone);
}
