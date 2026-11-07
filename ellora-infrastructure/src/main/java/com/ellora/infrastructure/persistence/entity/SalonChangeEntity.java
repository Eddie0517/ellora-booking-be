package com.ellora.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salon_profile_changes")
@Getter
@Setter
public class SalonChangeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "proposed", nullable = false, columnDefinition = "jsonb")
    private String proposedJson;

    @Column(name = "kind", nullable = false, length = 20)
    private String kind;

    @Column(name = "previous_logo_url", columnDefinition = "TEXT")
    private String previousLogoUrl;

    @Column(name = "proposed_logo_url", columnDefinition = "TEXT")
    private String proposedLogoUrl;

    @Column(name = "previous_image_urls", columnDefinition = "jsonb")
    private String previousImageUrlsJson;

    @Column(name = "proposed_image_urls", columnDefinition = "jsonb")
    private String proposedImageUrlsJson;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;
}
