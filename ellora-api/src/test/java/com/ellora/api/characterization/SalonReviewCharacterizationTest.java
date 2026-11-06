package com.ellora.api.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Characterization test for Salon Review Submission flow (US5 - T042).
 * Verifies review submission, rating 1-5 validation, unique booking review constraint, and salon reply.
 */
class SalonReviewCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should submit review with rating between 1 and 5 and record customer comment")
    void shouldSubmitReviewSuccessfully() {
        UUID ownerId = insertUser(
                "owner_rev_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0955111000", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_rev_" + UUID.randomUUID() + "@example.com", "hash", "Customer", "0955111111", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Review Star Salon", "80 Tran Phu", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Ombre Nails", new BigDecimal("320000"), 50);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),
                50,
                new BigDecimal("320000"),
                "COMPLETED");

        Long reviewId = jdbcTemplate.queryForObject(
                "INSERT INTO reviews (customer_id, salon_id, booking_id, rating, comment) "
                        + "VALUES (?, ?, ?, 5, 'Exceptional experience and gorgeous nail design!') RETURNING id",
                Long.class,
                customerId,
                salonId,
                bookingId);

        assertNotNull(reviewId);

        Map<String, Object> review = jdbcTemplate.queryForMap(
                "SELECT customer_id, salon_id, booking_id, rating, comment, created_at FROM reviews WHERE id = ?",
                reviewId);

        assertEquals(customerId, review.get("customer_id"));
        assertEquals(salonId, ((Number) review.get("salon_id")).longValue());
        assertEquals(bookingId, ((Number) review.get("booking_id")).longValue());
        assertEquals(5, ((Number) review.get("rating")).intValue());
        assertEquals("Exceptional experience and gorgeous nail design!", review.get("comment"));
        assertNotNull(review.get("created_at"));
    }

    @Test
    @DisplayName("Should reject review submission with rating outside 1 to 5 range")
    void shouldRejectInvalidRatingRange() {
        UUID ownerId = insertUser(
                "owner_rev2_" + UUID.randomUUID() + "@example.com", "hash", "Owner 2", "0955111222", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_rev2_" + UUID.randomUUID() + "@example.com", "hash", "Customer 2", "0955111333", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Rating Salon", "82 Tran Phu", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Standard Mani", new BigDecimal("150000"), 30);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),
                30,
                new BigDecimal("150000"),
                "COMPLETED");

        // Rating > 5 check
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "INSERT INTO reviews (customer_id, salon_id, booking_id, rating, comment) VALUES (?, ?, ?, 6, 'Over rating')",
                        customerId,
                        salonId,
                        bookingId),
                "Rating above 5 must violate CHECK constraint");
    }

    @Test
    @DisplayName("Should enforce one review per booking constraint (unique booking_id)")
    void shouldEnforceOneReviewPerBooking() {
        UUID ownerId = insertUser(
                "owner_rev3_" + UUID.randomUUID() + "@example.com", "hash", "Owner 3", "0955111444", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_rev3_" + UUID.randomUUID() + "@example.com", "hash", "Customer 3", "0955111555", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Unique Review Salon", "84 Tran Phu", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Standard Pedicure", new BigDecimal("180000"), 40);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2),
                40,
                new BigDecimal("180000"),
                "COMPLETED");

        // First review succeeds
        jdbcTemplate.update(
                "INSERT INTO reviews (customer_id, salon_id, booking_id, rating, comment) VALUES (?, ?, ?, 4, 'Good service')",
                customerId,
                salonId,
                bookingId);

        // Second review for the same booking must fail
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "INSERT INTO reviews (customer_id, salon_id, booking_id, rating, comment) VALUES (?, ?, ?, 5, 'Second review attempt')",
                        customerId,
                        salonId,
                        bookingId),
                "Submitting a duplicate review for the same booking must violate UNIQUE constraint on booking_id");
    }

    @Test
    @DisplayName("Should allow salon owner to reply to a submitted review")
    void shouldAllowSalonOwnerReply() {
        UUID ownerId = insertUser(
                "owner_rev4_" + UUID.randomUUID() + "@example.com", "hash", "Owner 4", "0955111666", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_rev4_" + UUID.randomUUID() + "@example.com", "hash", "Customer 4", "0955111777", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Reply Salon", "86 Tran Phu", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Express Polish", new BigDecimal("120000"), 25);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(3),
                25,
                new BigDecimal("120000"),
                "COMPLETED");

        Long reviewId = jdbcTemplate.queryForObject(
                "INSERT INTO reviews (customer_id, salon_id, booking_id, rating, comment) VALUES (?, ?, ?, 4, 'Nice ambiance') RETURNING id",
                Long.class,
                customerId,
                salonId,
                bookingId);

        String replyMessage = "Thank you so much for visiting! We look forward to seeing you again.";
        int updated = jdbcTemplate.update(
                "UPDATE reviews SET salon_reply = ?, salon_replied_at = CURRENT_TIMESTAMP WHERE id = ?",
                replyMessage,
                reviewId);
        assertEquals(1, updated);

        Map<String, Object> review =
                jdbcTemplate.queryForMap("SELECT salon_reply, salon_replied_at FROM reviews WHERE id = ?", reviewId);

        assertEquals(replyMessage, review.get("salon_reply"));
        assertNotNull(review.get("salon_replied_at"));
    }
}
