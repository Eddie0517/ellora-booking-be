package com.ellora.infrastructure.persistence.mapper;

import com.ellora.domain.model.Review;
import com.ellora.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toDomain(ReviewEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Review(
                entity.getId(),
                entity.getCustomerId(),
                entity.getSalonId(),
                entity.getBookingId(),
                entity.getRating(),
                entity.getComment(),
                entity.getSalonReply(),
                entity.getSalonRepliedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ReviewEntity toEntity(Review domain) {
        if (domain == null) {
            return null;
        }

        return ReviewEntity.builder()
                .id(domain.getId())
                .customerId(domain.getCustomerId())
                .salonId(domain.getSalonId())
                .bookingId(domain.getBookingId())
                .rating(domain.getRating())
                .comment(domain.getComment())
                .salonReply(domain.getSalonReply())
                .salonRepliedAt(domain.getSalonRepliedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
