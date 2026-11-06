package com.ellora.api.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Characterization test for Owner Booking Management flow (US5 - T043).
 * Verifies owner salon bookings listing, status filtering, and multi-tenant salon isolation.
 */
class OwnerBookingCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should list and filter bookings by status for a specific owner salon")
    void shouldListAndFilterBookingsByStatusForOwnerSalon() {
        UUID ownerId = insertUser(
                "owner_mgmt_" + UUID.randomUUID() + "@example.com", "hash", "Main Owner", "0966000111", "SALON_OWNER");
        UUID customer1 = insertUser(
                "cust_mgmt1_" + UUID.randomUUID() + "@example.com", "hash", "Customer One", "0966000222", "CUSTOMER");
        UUID customer2 = insertUser(
                "cust_mgmt2_" + UUID.randomUUID() + "@example.com", "hash", "Customer Two", "0966000333", "CUSTOMER");

        Long salonId = insertSalon(ownerId, "Royal Nails Spa", "20 Vo Van Kiet", "Da Nang", "APPROVED");
        Long serviceId = insertService(salonId, null, "Gel Manicure Deluxe", new BigDecimal("280000"), 45);
        Long employeeId = insertEmployee(salonId, "Technician Linh", "0966000444");

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Booking 1: PENDING
        Long b1 = insertBooking(
                customer1, salonId, serviceId, employeeId, now.plusHours(2), 45, new BigDecimal("280000"), "PENDING");

        // Booking 2: CONFIRMED
        Long b2 = insertBooking(
                customer2, salonId, serviceId, employeeId, now.plusHours(4), 45, new BigDecimal("280000"), "CONFIRMED");

        // Booking 3: COMPLETED
        Long b3 = insertGuestBooking(
                "Guest Walk-in",
                "0977888999",
                "guest@example.com",
                salonId,
                serviceId,
                employeeId,
                now.minusHours(3),
                45,
                new BigDecimal("280000"),
                "COMPLETED");

        // 1. Query all bookings for this salon ordered by scheduled_at
        List<Map<String, Object>> allSalonBookings = jdbcTemplate.queryForList(
                "SELECT b.id, b.status, b.scheduled_at, b.service_price_snapshot, "
                        + "COALESCE(u.full_name, b.walk_in_name) AS client_name, "
                        + "ns.name AS service_name, e.full_name AS employee_name "
                        + "FROM bookings b "
                        + "JOIN salons s ON s.id = b.salon_id "
                        + "JOIN nail_services ns ON ns.id = b.service_id "
                        + "LEFT JOIN users u ON u.id = b.customer_id "
                        + "LEFT JOIN employees e ON e.id = b.employee_id "
                        + "WHERE s.id = ? AND s.owner_id = ? "
                        + "ORDER BY b.scheduled_at ASC",
                salonId,
                ownerId);

        assertEquals(3, allSalonBookings.size(), "Owner must see all 3 bookings for their salon");

        // 2. Query filtered by status = 'PENDING'
        List<Map<String, Object>> pendingBookings = jdbcTemplate.queryForList(
                "SELECT b.id, b.status, COALESCE(u.full_name, b.walk_in_name) AS client_name "
                        + "FROM bookings b "
                        + "JOIN salons s ON s.id = b.salon_id "
                        + "LEFT JOIN users u ON u.id = b.customer_id "
                        + "WHERE s.id = ? AND s.owner_id = ? AND b.status = 'PENDING'",
                salonId,
                ownerId);

        assertEquals(1, pendingBookings.size());
        assertEquals(b1, ((Number) pendingBookings.get(0).get("id")).longValue());
        assertEquals("Customer One", pendingBookings.get(0).get("client_name"));

        // 3. Query filtered by status = 'COMPLETED'
        List<Map<String, Object>> completedBookings = jdbcTemplate.queryForList(
                "SELECT b.id, b.status, COALESCE(u.full_name, b.walk_in_name) AS client_name "
                        + "FROM bookings b "
                        + "JOIN salons s ON s.id = b.salon_id "
                        + "LEFT JOIN users u ON u.id = b.customer_id "
                        + "WHERE s.id = ? AND s.owner_id = ? AND b.status = 'COMPLETED'",
                salonId,
                ownerId);

        assertEquals(1, completedBookings.size());
        assertEquals(b3, ((Number) completedBookings.get(0).get("id")).longValue());
        assertEquals("Guest Walk-in", completedBookings.get(0).get("client_name"));
    }

    @Test
    @DisplayName("Should isolate bookings across different salons and owners")
    void shouldEnforceSalonOwnerIsolation() {
        UUID owner1 = insertUser(
                "owner_iso1_" + UUID.randomUUID() + "@example.com", "hash", "Owner 1", "0966111001", "SALON_OWNER");
        UUID owner2 = insertUser(
                "owner_iso2_" + UUID.randomUUID() + "@example.com", "hash", "Owner 2", "0966111002", "SALON_OWNER");

        Long salon1 = insertSalon(owner1, "Salon One", "Address 1", "Da Nang", "APPROVED");
        Long salon2 = insertSalon(owner2, "Salon Two", "Address 2", "Da Nang", "APPROVED");

        Long service1 = insertService(salon1, null, "Service 1", new BigDecimal("100000"), 30);
        Long service2 = insertService(salon2, null, "Service 2", new BigDecimal("100000"), 30);

        insertGuestBooking(
                "Guest S1",
                "0900000001",
                "s1@ex.com",
                salon1,
                service1,
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                30,
                new BigDecimal("100000"),
                "PENDING");
        insertGuestBooking(
                "Guest S2",
                "0900000002",
                "s2@ex.com",
                salon2,
                service2,
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                30,
                new BigDecimal("100000"),
                "PENDING");

        List<Map<String, Object>> owner1Bookings = jdbcTemplate.queryForList(
                "SELECT b.id FROM bookings b JOIN salons s ON s.id = b.salon_id WHERE s.id = ? AND s.owner_id = ?",
                salon1,
                owner1);

        assertEquals(1, owner1Bookings.size(), "Owner 1 query must only return Salon 1 bookings");

        List<Map<String, Object>> crossQueryBookings = jdbcTemplate.queryForList(
                "SELECT b.id FROM bookings b JOIN salons s ON s.id = b.salon_id WHERE s.id = ? AND s.owner_id = ?",
                salon2,
                owner1);

        assertTrue(crossQueryBookings.isEmpty(), "Owner 1 must see 0 bookings when querying Salon 2");
    }
}
