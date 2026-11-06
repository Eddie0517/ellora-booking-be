package com.ellora.api.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Characterization test for Guest Booking flow (US5 - T040).
 * Verifies that guest bookings allow customer_id=null while capturing guest contact details.
 */
class GuestBookingCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should create guest booking with null customer_id and populated guest contact details")
    void shouldCreateGuestBookingSuccessfully() {
        UUID ownerId = insertUser(
                "owner_guest_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0933111222", "SALON_OWNER");
        Long salonId = insertSalon(ownerId, "Guest Nail Bar", "100 Nguyen Van Linh", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Quick Polish", new BigDecimal("100000"), 20);

        String guestName = "Guest Tran Thi B";
        String guestPhone = "0944555666";
        String guestEmail = "guest_b_" + UUID.randomUUID() + "@example.com";
        OffsetDateTime scheduledAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        Long bookingId = insertGuestBooking(
                guestName,
                guestPhone,
                guestEmail,
                salonId,
                serviceId,
                null,
                scheduledAt,
                20,
                new BigDecimal("100000"),
                "PENDING");

        assertNotNull(bookingId);

        Map<String, Object> booking = jdbcTemplate.queryForMap(
                "SELECT customer_id, walk_in_name, walk_in_phone, guest_email, salon_id, status FROM bookings WHERE id = ?",
                bookingId);

        assertNull(booking.get("customer_id"), "Guest booking must have NULL customer_id");
        assertEquals(guestName, booking.get("walk_in_name"));
        assertEquals(guestPhone, booking.get("walk_in_phone"));
        assertEquals(guestEmail, booking.get("guest_email"));
        assertEquals("PENDING", booking.get("status"));
    }

    @Test
    @DisplayName("Should retrieve guest bookings by guest phone number")
    void shouldFindGuestBookingByPhone() {
        UUID ownerId = insertUser(
                "owner_phone_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0933111333", "SALON_OWNER");
        Long salonId = insertSalon(ownerId, "Phone Search Salon", "102 Nguyen Van Linh", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Gel Art", new BigDecimal("300000"), 50);

        String uniquePhone = "0987" + (int) (Math.random() * 900000 + 100000);
        insertGuestBooking(
                "Guest Customer",
                uniquePhone,
                "guest@example.com",
                salonId,
                serviceId,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(5),
                50,
                new BigDecimal("300000"),
                "PENDING");

        List<Map<String, Object>> bookings = jdbcTemplate.queryForList(
                "SELECT id, walk_in_name, service_price_snapshot FROM bookings WHERE walk_in_phone = ?", uniquePhone);

        assertEquals(1, bookings.size());
        assertEquals("Guest Customer", bookings.get(0).get("walk_in_name"));
    }

    @Test
    @DisplayName("Should enforce non-null salon_id foreign key constraint on guest booking")
    void shouldEnforceSalonConstraintForGuests() {
        Long invalidSalonId = 77777777L;

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertGuestBooking(
                        "Guest X",
                        "0900000000",
                        "guestx@example.com",
                        invalidSalonId,
                        1L,
                        null,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        30,
                        new BigDecimal("100000"),
                        "PENDING"));
    }
}
