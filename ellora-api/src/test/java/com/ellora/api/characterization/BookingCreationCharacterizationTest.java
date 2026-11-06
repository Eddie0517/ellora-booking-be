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
 * Characterization test for Booking Creation flow (US5 - T039).
 * Verifies booking creation, price snapshot integrity, and foreign key constraints on the `bookings` table.
 */
class BookingCreationCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should create booking for registered customer with PENDING default status and valid snapshot")
    void shouldCreateBookingForRegisteredCustomer() {
        UUID ownerId =
                insertUser("owner_" + UUID.randomUUID() + "@example.com", "hash", "Owner", "0911000111", "SALON_OWNER");
        UUID customerId =
                insertUser("cust_" + UUID.randomUUID() + "@example.com", "hash", "Customer", "0911000222", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Beauty Studio", "12 Le Duan", "Da Nang", "APPROVED");
        Long categoryId = insertCategory("Pedicure_" + UUID.randomUUID());
        Long serviceId = insertService(salonId, categoryId, "Spa Pedicure", new BigDecimal("250000"), 60);
        Long employeeId = insertEmployee(salonId, "Staff Member", "0911000333");

        OffsetDateTime scheduledAt = OffsetDateTime.of(2026, 12, 1, 14, 0, 0, 0, ZoneOffset.UTC);
        BigDecimal priceSnapshot = new BigDecimal("250000");

        Long bookingId =
                insertBooking(customerId, salonId, serviceId, employeeId, scheduledAt, 60, priceSnapshot, "PENDING");

        assertNotNull(bookingId);

        Map<String, Object> booking = jdbcTemplate.queryForMap(
                "SELECT id, customer_id, salon_id, service_id, employee_id, scheduled_at, duration_minutes, "
                        + "service_price_snapshot, status, created_at, updated_at FROM bookings WHERE id = ?",
                bookingId);

        assertEquals(bookingId, ((Number) booking.get("id")).longValue());
        assertEquals(customerId, booking.get("customer_id"));
        assertEquals(salonId, ((Number) booking.get("salon_id")).longValue());
        assertEquals(serviceId, ((Number) booking.get("service_id")).longValue());
        assertEquals(employeeId, ((Number) booking.get("employee_id")).longValue());
        assertEquals(60, ((Number) booking.get("duration_minutes")).intValue());
        assertEquals(
                priceSnapshot,
                new BigDecimal(booking.get("service_price_snapshot").toString()));
        assertEquals("PENDING", booking.get("status"));
        assertNotNull(booking.get("created_at"));
        assertNotNull(booking.get("updated_at"));
    }

    @Test
    @DisplayName("Should preserve service price snapshot when service price is updated later")
    void shouldPreserveServicePriceSnapshot() {
        UUID ownerId = insertUser(
                "owner2_" + UUID.randomUUID() + "@example.com", "hash", "Owner 2", "0911000444", "SALON_OWNER");
        UUID customerId = insertUser(
                "cust2_" + UUID.randomUUID() + "@example.com", "hash", "Customer 2", "0911000555", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Nail Lounge", "45 Tran Phu", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Standard Mani", new BigDecimal("150000"), 30);

        OffsetDateTime scheduledAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2);
        Long bookingId = insertBooking(
                customerId, salonId, serviceId, null, scheduledAt, 30, new BigDecimal("150000"), "PENDING");

        // Service price changes in salon menu
        jdbcTemplate.update("UPDATE nail_services SET price = ? WHERE id = ?", new BigDecimal("200000"), serviceId);

        // Booking record should retain original price snapshot
        BigDecimal recordedSnapshot = jdbcTemplate.queryForObject(
                "SELECT service_price_snapshot FROM bookings WHERE id = ?", BigDecimal.class, bookingId);

        assertEquals(new BigDecimal("150000"), recordedSnapshot, "Snapshot must preserve historical price");
    }

    @Test
    @DisplayName("Should enforce foreign key constraint when salon_id does not exist")
    void shouldFailOnInvalidSalonForeignKey() {
        UUID customerId =
                insertUser("cust3_" + UUID.randomUUID() + "@example.com", "hash", "Cust 3", "0911000666", "CUSTOMER");
        UUID ownerId = insertUser(
                "owner3_" + UUID.randomUUID() + "@example.com", "hash", "Owner 3", "0911000777", "SALON_OWNER");
        Long salonId = insertSalon(ownerId, "Test Salon", "Address", "City", "APPROVED");
        Long serviceId = insertService(salonId, null, "Test Service", new BigDecimal("100000"), 30);

        Long invalidSalonId = 99999999L;
        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertBooking(
                        customerId,
                        invalidSalonId,
                        serviceId,
                        null,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        30,
                        new BigDecimal("100000"),
                        "PENDING"));
    }

    @Test
    @DisplayName("Should enforce foreign key constraint when service_id does not exist")
    void shouldFailOnInvalidServiceForeignKey() {
        UUID ownerId = insertUser(
                "owner4_" + UUID.randomUUID() + "@example.com", "hash", "Owner 4", "0911000888", "SALON_OWNER");
        UUID customerId =
                insertUser("cust4_" + UUID.randomUUID() + "@example.com", "hash", "Cust 4", "0911000999", "CUSTOMER");
        Long salonId = insertSalon(ownerId, "Test Salon 4", "Address", "City", "APPROVED");

        Long invalidServiceId = 88888888L;
        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertBooking(
                        customerId,
                        salonId,
                        invalidServiceId,
                        null,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        30,
                        new BigDecimal("100000"),
                        "PENDING"));
    }
}
