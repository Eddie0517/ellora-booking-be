package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.infrastructure.persistence.entity.BookingEntity;
import com.ellora.infrastructure.persistence.mapper.BookingMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataBookingRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final SpringDataBookingRepository springDataBookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = bookingMapper.toEntity(booking);
        BookingEntity saved = springDataBookingRepository.save(entity);
        return bookingMapper.toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return springDataBookingRepository.findById(id).map(bookingMapper::toDomain);
    }

    @Override
    public List<Booking> findByCustomerId(UUID customerId) {
        return springDataBookingRepository.findByCustomerIdOrderByScheduledAtDesc(customerId)
                .stream()
                .map(bookingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findBySalonId(Long salonId) {
        return springDataBookingRepository.findBySalonIdOrderByScheduledAtAsc(salonId)
                .stream()
                .map(bookingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findBySalonIdAndStatus(Long salonId, BookingStatus status) {
        return springDataBookingRepository.findBySalonIdAndStatusOrderByScheduledAtAsc(salonId, status.name())
                .stream()
                .map(bookingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findBySalonIdAndScheduledAtBetween(Long salonId, Instant start, Instant end) {
        return springDataBookingRepository.findBySalonIdAndScheduledAtBetweenOrderByScheduledAtAsc(salonId, start, end)
                .stream()
                .map(bookingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findByWalkInPhone(String phone) {
        return springDataBookingRepository.findByWalkInPhoneOrderByScheduledAtDesc(phone)
                .stream()
                .map(bookingMapper::toDomain)
                .toList();
    }
}
