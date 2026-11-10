package com.ellora.domain.model;

import com.ellora.domain.constant.SalonStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Salon {

    private Long id;
    private UUID ownerId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String district;
    private String phone;
    private String email;
    private String logoUrl;
    private SalonStatus status;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String subscriptionTier;
    private Instant subscriptionStartsAt;
    private Instant subscriptionExpiresAt;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();
    private String contractUrl;
    private String contractFileName;
    private String contractFileType;
    private Instant contractUploadedAt;
    private boolean overdue;
    private Instant createdAt;
    private Instant updatedAt;

    public Salon() {}

    public Salon(Long id, UUID ownerId, String name, String description, String address,
                 String city, String district, String phone, String email, String logoUrl,
                 SalonStatus status, Double latitude, Double longitude, String subscriptionTier,
                 Instant subscriptionStartsAt, Instant subscriptionExpiresAt, BigDecimal averageRating,
                 Integer totalReviews, Instant createdAt, Instant updatedAt) {
        this(id, ownerId, name, description, address, city, district, phone, email, logoUrl,
                status, latitude, longitude, subscriptionTier, subscriptionStartsAt, subscriptionExpiresAt,
                averageRating, totalReviews, new java.util.ArrayList<>(), createdAt, updatedAt);
    }

    public Salon(Long id, UUID ownerId, String name, String description, String address,
                 String city, String district, String phone, String email, String logoUrl,
                 SalonStatus status, Double latitude, Double longitude, String subscriptionTier,
                 Instant subscriptionStartsAt, Instant subscriptionExpiresAt, BigDecimal averageRating,
                 Integer totalReviews, java.util.List<String> imageUrls, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.district = district;
        this.phone = phone;
        this.email = email;
        this.logoUrl = logoUrl;
        this.status = status != null ? status : SalonStatus.PENDING_APPROVAL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.subscriptionTier = subscriptionTier;
        this.subscriptionStartsAt = subscriptionStartsAt;
        this.subscriptionExpiresAt = subscriptionExpiresAt;
        this.averageRating = averageRating != null ? averageRating : BigDecimal.ZERO;
        this.totalReviews = totalReviews != null ? totalReviews : 0;
        this.imageUrls = imageUrls != null ? new java.util.ArrayList<>(imageUrls) : new java.util.ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Salon createNew(UUID ownerId, String name, String address, String city, String district, String phone, String email, String description) {
        return createNew(ownerId, name, address, city, district, phone, email, description, null, null);
    }

    public static Salon createNew(UUID ownerId, String name, String address, String city, String district, String phone, String email, String description, Double latitude, Double longitude) {
        Instant now = Instant.now();
        return new Salon(null, ownerId, name, description, address, city, district, phone, email, null,
                SalonStatus.PENDING_APPROVAL, latitude, longitude, null, null, null, BigDecimal.ZERO, 0, now, now);
    }

    public boolean isActive() {
        return this.status == SalonStatus.ACTIVE;
    }

    public void approve() {
        this.status = SalonStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void approveWithContract(String contractUrl, String contractFileName, String contractFileType) {
        this.status = SalonStatus.ACTIVE;
        this.contractUrl = contractUrl;
        this.contractFileName = contractFileName;
        this.contractFileType = contractFileType;
        this.contractUploadedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void suspend() {
        this.status = SalonStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void updateRatingStats(BigDecimal averageRating, int totalReviews) {
        this.averageRating = averageRating != null ? averageRating : BigDecimal.ZERO;
        this.totalReviews = totalReviews;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String name, String description, String address, String city, String district,
                              String phone, String email, String logoUrl) {
        updateProfile(name, description, address, city, district, phone, email, logoUrl, this.latitude, this.longitude);
    }

    public void updateProfile(String name, String description, String address, String city, String district,
                              String phone, String email, String logoUrl, Double latitude, Double longitude) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (description != null) this.description = description.trim();
        if (address != null && !address.isBlank()) this.address = address.trim();
        if (city != null && !city.isBlank()) this.city = city.trim();
        if (district != null && !district.isBlank()) this.district = district.trim();
        if (phone != null && !phone.isBlank()) this.phone = phone.trim();
        if (email != null && !email.isBlank()) this.email = email.trim();
        if (logoUrl != null) this.logoUrl = logoUrl.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public SalonStatus getStatus() { return status; }
    public void setStatus(SalonStatus status) { this.status = status; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public Instant getSubscriptionStartsAt() { return subscriptionStartsAt; }
    public void setSubscriptionStartsAt(Instant subscriptionStartsAt) { this.subscriptionStartsAt = subscriptionStartsAt; }

    public Instant getSubscriptionExpiresAt() { return subscriptionExpiresAt; }
    public void setSubscriptionExpiresAt(Instant subscriptionExpiresAt) { this.subscriptionExpiresAt = subscriptionExpiresAt; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public java.util.List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(java.util.List<String> imageUrls) { this.imageUrls = imageUrls != null ? new java.util.ArrayList<>(imageUrls) : new java.util.ArrayList<>(); }

    public String getContractUrl() { return contractUrl; }
    public void setContractUrl(String contractUrl) { this.contractUrl = contractUrl; }

    public String getContractFileName() { return contractFileName; }
    public void setContractFileName(String contractFileName) { this.contractFileName = contractFileName; }

    public String getContractFileType() { return contractFileType; }
    public void setContractFileType(String contractFileType) { this.contractFileType = contractFileType; }

    public Instant getContractUploadedAt() { return contractUploadedAt; }
    public void setContractUploadedAt(Instant contractUploadedAt) { this.contractUploadedAt = contractUploadedAt; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
