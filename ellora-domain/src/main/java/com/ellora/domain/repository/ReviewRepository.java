package com.ellora.domain.repository;

import com.ellora.domain.model.Review;
import java.util.List;
import java.util.Optional;

/**
 * Domain port for managing salon and booking reviews.
 */
public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    List<Review> findBySalonId(Long salonId, int page, int pageSize);

    long countBySalonId(Long salonId);

    void delete(Long id);

    Double calculateAverageRatingBySalonId(Long salonId);
}
