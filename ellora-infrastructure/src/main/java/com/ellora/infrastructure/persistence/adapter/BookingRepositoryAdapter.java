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
import org.springframework.data.domain.PageRequest;
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
        return springDataBookingRepository.findByCustomerIdAndCustomerHiddenAtIsNullOrderByScheduledAtDesc(customerId)
                .stream()
                .map(bookingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findByCustomerId(UUID customerId, int page, int pageSize) {
        return springDataBookingRepository.findByCustomerIdAndCustomerHiddenAtIsNullOrderByScheduledAtDesc(customerId, PageRequest.of(page, pageSize))
                .stream().map(bookingMapper::toDomain).toList();
    }

    @Override
    public long countByCustomerId(UUID customerId) {
        return springDataBookingRepository.countByCustomerIdAndCustomerHiddenAtIsNull(customerId);
    }

    @Override
    public void hideCancelledForCustomer(Long bookingId, UUID customerId) {
        springDataBookingRepository.hideCancelledForCustomer(bookingId, customerId, Instant.now());
    }

    @Override
    public List<Booking> findDisputes(String status, int page, int pageSize) {
        return springDataBookingRepository.findDisputes(status, PageRequest.of(page, pageSize))
                .stream().map(bookingMapper::toDomain).toList();
    }

    @Override
    public long countDisputes(String status) {
        return springDataBookingRepository.findDisputes(status, PageRequest.of(0, 1)).getTotalElements();
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

    @Override
    public long countBySalonId(Long salonId) {
        return springDataBookingRepository.countBySalonId(salonId);
    }

    @Override
    public long countBySalonIdAndStatus(Long salonId, BookingStatus status) {
        return springDataBookingRepository.countBySalonIdAndStatus(salonId, status != null ? status.name() : null);
    }

    @Override
    public java.math.BigDecimal sumCompletedGrossBySalonIdAndPeriod(Long salonId, Instant start, Instant end) {
        return springDataBookingRepository.sumCompletedGrossBySalonIdAndPeriod(salonId, start, end);
    }

    @Override
    public long countDistinctCompletedCustomersBySalonIdAndPeriod(Long salonId, Instant start, Instant end) {
        Long count = springDataBookingRepository.countDistinctCompletedCustomersBySalonIdAndPeriod(salonId, start, end);
        return count != null ? count : 0L;
    }
}
