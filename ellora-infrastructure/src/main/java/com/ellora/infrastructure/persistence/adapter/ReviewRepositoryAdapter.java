package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.Review;
import com.ellora.domain.repository.ReviewRepository;
import com.ellora.infrastructure.persistence.entity.ReviewEntity;
import com.ellora.infrastructure.persistence.mapper.ReviewMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataReviewRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final SpringDataReviewRepository springDataReviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public Review save(Review review) {
        ReviewEntity entity = reviewMapper.toEntity(review);
        ReviewEntity saved = springDataReviewRepository.save(entity);
        return reviewMapper.toDomain(saved);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return springDataReviewRepository.findById(id).map(reviewMapper::toDomain);
    }

    @Override
    public Optional<Review> findByBookingId(Long bookingId) {
        return springDataReviewRepository.findByBookingId(bookingId).map(reviewMapper::toDomain);
    }

    @Override
    public boolean existsByBookingId(Long bookingId) {
        return springDataReviewRepository.existsByBookingId(bookingId);
    }

    @Override
    public List<Review> findBySalonId(Long salonId, int page, int pageSize) {
        return springDataReviewRepository.findBySalonIdOrderByCreatedAtDesc(salonId, PageRequest.of(page, pageSize))
                .getContent()
                .stream()
                .map(reviewMapper::toDomain)
                .toList();
    }

    @Override
    public long countBySalonId(Long salonId) {
        return springDataReviewRepository.countBySalonId(salonId);
    }

    @Override
    public Double calculateAverageRatingBySalonId(Long salonId) {
        return springDataReviewRepository.calculateAverageRatingBySalonId(salonId);
    }
}
