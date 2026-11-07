package com.ellora.domain.repository;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.model.Booking;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

    Booking save(Booking booking);

    List<Booking> findAll();

    Optional<Booking> findById(Long id);

    List<Booking> findByCustomerId(UUID customerId);

    List<Booking> findByCustomerId(UUID customerId, int page, int pageSize);

    long countByCustomerId(UUID customerId);

    void hideCancelledForCustomer(Long bookingId, UUID customerId);
    List<Booking> findDisputes(String status, int page, int pageSize);
    long countDisputes(String status);

    List<Booking> findBySalonId(Long salonId);

    List<Booking> findBySalonIdAndStatus(Long salonId, BookingStatus status);

    List<Booking> findBySalonIdAndScheduledAtBetween(Long salonId, Instant start, Instant end);

    List<Booking> findByWalkInPhone(String phone);

    long countBySalonId(Long salonId);

    long countBySalonIdAndStatus(Long salonId, BookingStatus status);

    java.math.BigDecimal sumCompletedGrossBySalonIdAndPeriod(Long salonId, Instant start, Instant end);

    long countDistinctCompletedCustomersBySalonIdAndPeriod(Long salonId, Instant start, Instant end);
}
