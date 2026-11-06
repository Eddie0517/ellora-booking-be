package com.ellora.domain.repository;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.model.Booking;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByCustomerId(UUID customerId);

    List<Booking> findBySalonId(Long salonId);

    List<Booking> findBySalonIdAndStatus(Long salonId, BookingStatus status);

    List<Booking> findBySalonIdAndScheduledAtBetween(Long salonId, Instant start, Instant end);

    List<Booking> findByWalkInPhone(String phone);
}
