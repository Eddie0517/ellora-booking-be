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
 * Characterization test for Booking Status Transition flow (US5 - T041).
 * Verifies status updates (PENDING -> CONFIRMED -> COMPLETED / CANCELLED) and completion disputes.
 */
class BookingStatusTransitionCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should transition booking from PENDING to CONFIRMED and then to COMPLETED with completed_at")
    void shouldTransitionBookingLifecycle() {
        UUID ownerId = insertUser(
                "owner_st_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0922111000", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_st_" + UUID.randomUUID() + "@example.com", "hash", "Customer", "0922111111", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Transition Nails", "50 Hung Vuong", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Full Set Acrylic", new BigDecimal("400000"), 60);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),
                60,
                new BigDecimal("400000"),
                "PENDING");

        // 1. Confirm booking
        int confirmedRows = jdbcTemplate.update(
                "UPDATE bookings SET status = 'CONFIRMED', updated_at = CURRENT_TIMESTAMP WHERE id = ?", bookingId);
        assertEquals(1, confirmedRows);

        String statusAfterConfirm =
                jdbcTemplate.queryForObject("SELECT status FROM bookings WHERE id = ?", String.class, bookingId);
        assertEquals("CONFIRMED", statusAfterConfirm);

        // 2. Complete booking with completed_at timestamp and confirmation note
        OffsetDateTime completedAt = OffsetDateTime.now(ZoneOffset.UTC);
        int completedRows = jdbcTemplate.update(
                "UPDATE bookings SET status = 'COMPLETED', completed_at = ?, completion_confirmation = 'CUSTOMER_CONFIRMED', "
                        + "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                completedAt,
                bookingId);
        assertEquals(1, completedRows);

        Map<String, Object> finalBooking = jdbcTemplate.queryForMap(
                "SELECT status, completed_at, completion_confirmation FROM bookings WHERE id = ?", bookingId);

        assertEquals("COMPLETED", finalBooking.get("status"));
        assertNotNull(finalBooking.get("completed_at"));
        assertEquals("CUSTOMER_CONFIRMED", finalBooking.get("completion_confirmation"));
    }

    @Test
    @DisplayName("Should cancel pending booking and record cancellation reason")
    void shouldCancelBookingWithReason() {
        UUID ownerId = insertUser(
                "owner_can_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0922111222", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_can_" + UUID.randomUUID() + "@example.com", "hash", "Customer", "0922111333", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Cancel Nails", "52 Hung Vuong", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Basic Polish", new BigDecimal("100000"), 30);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(2),
                30,
                new BigDecimal("100000"),
                "PENDING");

        String reason = "Customer schedule changed unexpectedly";
        jdbcTemplate.update(
                "UPDATE bookings SET status = 'CANCELLED', cancellation_reason = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                reason,
                bookingId);

        Map<String, Object> booking =
                jdbcTemplate.queryForMap("SELECT status, cancellation_reason FROM bookings WHERE id = ?", bookingId);

        assertEquals("CANCELLED", booking.get("status"));
        assertEquals(reason, booking.get("cancellation_reason"));
    }

    @Test
    @DisplayName("Should record dispute in booking_completion_disputes and enforce unique booking+version constraint")
    void shouldRecordDisputeAndEnforceVersionConstraint() {
        UUID ownerId = insertUser(
                "owner_disp_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0922111444", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust_disp_" + UUID.randomUUID() + "@example.com", "hash", "Customer", "0922111555", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Dispute Nails", "54 Hung Vuong", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "French Tips", new BigDecimal("200000"), 40);

        Long bookingId = insertBooking(
                customerId,
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(2),
                40,
                new BigDecimal("200000"),
                "COMPLETED");

        // Record a dispute for version 0
        int inserted = jdbcTemplate.update(
                "INSERT INTO booking_completion_disputes (booking_id, completion_version, reason, status) "
                        + "VALUES (?, 0, 'Service did not meet expectations', 'OPEN')",
                bookingId);
        assertEquals(1, inserted);

        // Attempting to insert another dispute for the exact same (booking_id, completion_version) must violate
        // uq_booking_completion_dispute
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "INSERT INTO booking_completion_disputes (booking_id, completion_version, reason, status) "
                                + "VALUES (?, 0, 'Duplicate dispute reason', 'OPEN')",
                        bookingId));
    }
}
