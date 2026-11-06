package com.ellora.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_hidden_at")
    private Instant customerHiddenAt;

    @Column(name = "walk_in_name", length = 255)
    private String walkInName;

    @Column(name = "guest_email", length = 255)
    private String guestEmail;

    @Column(name = "walk_in_phone", length = 20)
    private String walkInPhone;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "service_price_snapshot", precision = 15, scale = 0)
    private BigDecimal servicePriceSnapshot;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completion_confirmation", length = 32)
    private String completionConfirmation;

    @Column(name = "completion_version", nullable = false)
    @Builder.Default
    private Long completionVersion = 0L;

    @Column(name = "completion_responded_at")
    private Instant completionRespondedAt;

    @Column(name = "completion_customer_note", length = 1000)
    private String completionCustomerNote;

    @Column(name = "completion_admin_note", length = 2000)
    private String completionAdminNote;

    @Column(name = "completion_resolution", length = 16)
    private String completionResolution;

    @Column(name = "completion_resolved_at")
    private Instant completionResolvedAt;

    @Column(name = "revenue_estimated", nullable = false)
    @Builder.Default
    private boolean revenueEstimated = false;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "salon_note", columnDefinition = "TEXT")
    private String salonNote;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
