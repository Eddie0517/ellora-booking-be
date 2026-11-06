package com.ellora.infrastructure.persistence.mapper;

import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.infrastructure.persistence.entity.EmployeeEntity;
import com.ellora.infrastructure.persistence.entity.NailServiceEntity;
import com.ellora.infrastructure.persistence.entity.SalonEntity;
import org.springframework.stereotype.Component;

@Component
public class SalonMapper {

    public Salon toDomain(SalonEntity entity) {
        if (entity == null) {
            return null;
        }

        SalonStatus status = SalonStatus.PENDING_APPROVAL;
        if (entity.getStatus() != null) {
            try {
                status = SalonStatus.valueOf(entity.getStatus());
            } catch (IllegalArgumentException ignored) {}
        }

        return new Salon(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getLogoUrl(),
                status,
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getSubscriptionTier(),
                entity.getSubscriptionStartsAt(),
                entity.getSubscriptionExpiresAt(),
                entity.getAverageRating(),
                entity.getTotalReviews(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public SalonEntity toEntity(Salon domain) {
        if (domain == null) {
            return null;
        }

        return SalonEntity.builder()
                .id(domain.getId())
                .ownerId(domain.getOwnerId())
                .name(domain.getName())
                .description(domain.getDescription())
                .address(domain.getAddress())
                .city(domain.getCity())
                .district(domain.getDistrict())
                .phone(domain.getPhone())
                .email(domain.getEmail())
                .logoUrl(domain.getLogoUrl())
                .status(domain.getStatus() != null ? domain.getStatus().name() : SalonStatus.PENDING_APPROVAL.name())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .subscriptionTier(domain.getSubscriptionTier())
                .subscriptionStartsAt(domain.getSubscriptionStartsAt())
                .subscriptionExpiresAt(domain.getSubscriptionExpiresAt())
                .averageRating(domain.getAverageRating())
                .totalReviews(domain.getTotalReviews())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public NailService toDomain(NailServiceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new NailService(
                entity.getId(),
                entity.getSalonId(),
                entity.getCategoryId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getDurationMinutes(),
                entity.getImageUrl(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public NailServiceEntity toEntity(NailService domain) {
        if (domain == null) {
            return null;
        }

        return NailServiceEntity.builder()
                .id(domain.getId())
                .salonId(domain.getSalonId())
                .categoryId(domain.getCategoryId())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .durationMinutes(domain.getDurationMinutes())
                .imageUrl(domain.getImageUrl())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Employee toDomain(EmployeeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Employee(
                entity.getId(),
                entity.getSalonId(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getAvatarUrl(),
                entity.getBio(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public EmployeeEntity toEntity(Employee domain) {
        if (domain == null) {
            return null;
        }

        return EmployeeEntity.builder()
                .id(domain.getId())
                .salonId(domain.getSalonId())
                .fullName(domain.getFullName())
                .phone(domain.getPhone())
                .avatarUrl(domain.getAvatarUrl())
                .bio(domain.getBio())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
