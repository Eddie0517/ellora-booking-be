package com.ellora.infrastructure.persistence.mapper;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.model.Booking;
import com.ellora.infrastructure.persistence.entity.BookingEntity;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public Booking toDomain(BookingEntity entity) {
        if (entity == null) {
            return null;
        }

        BookingStatus status = BookingStatus.PENDING;
        if (entity.getStatus() != null) {
            try {
                status = BookingStatus.valueOf(entity.getStatus());
            } catch (IllegalArgumentException ignored) {}
        }

        return new Booking(
                entity.getId(),
                entity.getCustomerId(),
                entity.getWalkInName(),
                entity.getGuestEmail(),
                entity.getWalkInPhone(),
                entity.getSalonId(),
                entity.getServiceId(),
                entity.getEmployeeId(),
                entity.getScheduledAt(),
                entity.getDurationMinutes(),
                entity.getServicePriceSnapshot(),
                status,
                entity.getCustomerNote(),
                entity.getSalonNote(),
                entity.getCancellationReason(),
                entity.getCompletedAt(),
                entity.getCompletionConfirmation(),
                entity.getCompletionVersion(),
                entity.getCompletionRespondedAt(),
                entity.getCompletionCustomerNote(),
                entity.getCompletionAdminNote(),
                entity.getCompletionResolvedAt(),
                entity.isRevenueEstimated(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public BookingEntity toEntity(Booking domain) {
        if (domain == null) {
            return null;
        }

        return BookingEntity.builder()
                .id(domain.getId())
                .customerId(domain.getCustomerId())
                .walkInName(domain.getWalkInName())
                .guestEmail(domain.getGuestEmail())
                .walkInPhone(domain.getWalkInPhone())
                .salonId(domain.getSalonId())
                .serviceId(domain.getServiceId())
                .employeeId(domain.getEmployeeId())
                .scheduledAt(domain.getScheduledAt())
                .durationMinutes(domain.getDurationMinutes())
                .servicePriceSnapshot(domain.getServicePriceSnapshot())
                .status(domain.getStatus() != null ? domain.getStatus().name() : "PENDING")
                .customerNote(domain.getCustomerNote())
                .salonNote(domain.getSalonNote())
                .cancellationReason(domain.getCancellationReason())
                .completedAt(domain.getCompletedAt())
                .completionConfirmation(domain.getCompletionConfirmation())
                .completionVersion(domain.getCompletionVersion() != null ? domain.getCompletionVersion() : 0L)
                .completionRespondedAt(domain.getCompletionRespondedAt())
                .completionCustomerNote(domain.getCompletionCustomerNote())
                .completionAdminNote(domain.getCompletionAdminNote())
                .completionResolvedAt(domain.getCompletionResolvedAt())
                .revenueEstimated(domain.isRevenueEstimated())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
