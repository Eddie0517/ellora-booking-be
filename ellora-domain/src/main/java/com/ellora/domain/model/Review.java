package com.ellora.domain.model;

import com.ellora.domain.exception.BusinessRuleException;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a customer review for a completed salon booking.
 */
public class Review {

    private Long id;
    private UUID customerId;
    private Long salonId;
    private Long bookingId;
    private int rating;
    private String comment;
    private String salonReply;
    private Instant salonRepliedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Review() {
    }

    public Review(Long id, UUID customerId, Long salonId, Long bookingId, int rating,
                  String comment, String salonReply, Instant salonRepliedAt,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.salonId = salonId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.salonReply = salonReply;
        this.salonRepliedAt = salonRepliedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Review create(UUID customerId, Long salonId, Long bookingId, int rating, String comment) {
        if (customerId == null) {
            throw new BusinessRuleException("INVALID_CUSTOMER", "Khách hàng đánh giá không được để trống");
        }
        if (salonId == null) {
            throw new BusinessRuleException("INVALID_SALON", "Salon được đánh giá không được để trống");
        }
        if (bookingId == null) {
            throw new BusinessRuleException("INVALID_BOOKING", "Lịch hẹn được đánh giá không được để trống");
        }
        if (rating < 1 || rating > 5) {
            throw new BusinessRuleException("INVALID_RATING", "Điểm đánh giá phải từ 1 đến 5 sao");
        }

        Instant now = Instant.now();
        return new Review(
                null,
                customerId,
                salonId,
                bookingId,
                rating,
                comment != null ? comment.trim() : null,
                null,
                null,
                now,
                now
        );
    }

    public void reply(String reply) {
        if (salonReply != null || salonRepliedAt != null) {
            throw new BusinessRuleException("ALREADY_REPLIED", "Phản hồi đã gửi không thể chỉnh sửa hoặc gửi lại");
        }
        if (reply == null || reply.isBlank()) {
            throw new BusinessRuleException("INVALID_REPLY", "Nội dung phản hồi không được để trống");
        }
        this.salonReply = reply.trim();
        this.salonRepliedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void adminEdit(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BusinessRuleException("INVALID_RATING", "Điểm đánh giá phải từ 1 đến 5 sao");
        }
        this.rating = rating;
        this.comment = comment != null ? comment.trim() : null;
        this.updatedAt = Instant.now();
    }

    public void adminReply(String reply) {
        this.salonReply = reply != null ? reply.trim() : null;
        this.salonRepliedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void adminDeleteReply() {
        this.salonReply = null;
        this.salonRepliedAt = null;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getSalonReply() { return salonReply; }
    public void setSalonReply(String salonReply) { this.salonReply = salonReply; }

    public Instant getSalonRepliedAt() { return salonRepliedAt; }
    public void setSalonRepliedAt(Instant salonRepliedAt) { this.salonRepliedAt = salonRepliedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
