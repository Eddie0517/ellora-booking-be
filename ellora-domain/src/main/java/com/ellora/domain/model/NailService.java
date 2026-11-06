package com.ellora.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class NailService {

    private Long id;
    private Long salonId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private String imageUrl;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public NailService() {}

    public NailService(Long id, Long salonId, Long categoryId, String name, String description,
                       BigDecimal price, Integer durationMinutes, String imageUrl, boolean active,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.salonId = salonId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.durationMinutes = durationMinutes != null ? durationMinutes : 30;
        this.imageUrl = imageUrl;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static NailService createNew(Long salonId, Long categoryId, String name, String description,
                                        BigDecimal price, Integer durationMinutes, String imageUrl) {
        Instant now = Instant.now();
        return new NailService(null, salonId, categoryId, name, description, price,
                durationMinutes != null ? durationMinutes : 30, imageUrl, true, now, now);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
