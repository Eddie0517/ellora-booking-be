package com.ellora.api.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Characterization test for Salon Search flow (US5 - T038).
 * Verifies searching approved salons by city, name keyword, and joining active nail services.
 */
class SalonSearchCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should find only approved salons by city and exclude pending approval salons")
    void shouldFindApprovedSalonsByCity() {
        UUID ownerId = insertUser(
                "owner_" + UUID.randomUUID() + "@example.com", "hash", "Salon Owner 1", "0988777666", "SALON_OWNER");

        String city = "Da Nang";
        Long approvedSalonId = insertSalon(ownerId, "Luxe Nail Bar", "123 Bach Dang", city, "APPROVED");

        Long pendingSalonId = insertSalon(ownerId, "Hidden Beauty", "456 Tran Phu", city, "PENDING_APPROVAL");

        Long otherCitySalonId = insertSalon(ownerId, "Saigon Glow", "789 Le Loi", "Ho Chi Minh", "APPROVED");

        List<Map<String, Object>> searchResults = jdbcTemplate.queryForList(
                "SELECT id, name, city, status FROM salons WHERE city = ? AND status = 'APPROVED'", city);

        assertEquals(1, searchResults.size(), "Only approved salon in Da Nang should match");
        assertEquals(approvedSalonId, ((Number) searchResults.get(0).get("id")).longValue());
        assertEquals("Luxe Nail Bar", searchResults.get(0).get("name"));
    }

    @Test
    @DisplayName("Should search approved salons by name substring case-insensitively")
    void shouldSearchApprovedSalonsByName() {
        UUID ownerId = insertUser(
                "owner2_" + UUID.randomUUID() + "@example.com", "hash", "Salon Owner 2", "0988777555", "SALON_OWNER");

        String uniquePrefix = UUID.randomUUID().toString().substring(0, 8);
        String salonName = uniquePrefix + "_Diamond Nails Studio";

        Long salonId = insertSalon(ownerId, salonName, "55 Nguyen Hue", "Da Nang", "APPROVED");

        List<Map<String, Object>> matches = jdbcTemplate.queryForList(
                "SELECT id, name FROM salons WHERE LOWER(name) LIKE LOWER(?) AND status = 'APPROVED'",
                "%" + uniquePrefix + "%");

        assertEquals(1, matches.size());
        assertEquals(salonId, ((Number) matches.get(0).get("id")).longValue());
    }

    @Test
    @DisplayName("Should join approved salon with its active categories and nail services")
    void shouldJoinSalonWithActiveServices() {
        UUID ownerId = insertUser(
                "owner3_" + UUID.randomUUID() + "@example.com", "hash", "Salon Owner 3", "0988777444", "SALON_OWNER");

        Long salonId = insertSalon(ownerId, "Elegance Nails & Spa", "99 Han Thuyen", "Da Nang", "APPROVED");

        Long categoryId = insertCategory("Manicure_" + UUID.randomUUID());
        Long service1Id = insertService(salonId, categoryId, "Gel Manicure Polish", new BigDecimal("180000"), 45);
        Long service2Id = insertService(salonId, categoryId, "Acrylic Full Set", new BigDecimal("350000"), 90);

        List<Map<String, Object>> salonServices = jdbcTemplate.queryForList(
                "SELECT s.name AS salon_name, c.name AS category_name, ns.name AS service_name, "
                        + "ns.price, ns.duration_minutes "
                        + "FROM salons s "
                        + "JOIN nail_services ns ON ns.salon_id = s.id "
                        + "LEFT JOIN service_categories c ON c.id = ns.category_id "
                        + "WHERE s.id = ? AND ns.active = true ORDER BY ns.price ASC",
                salonId);

        assertEquals(2, salonServices.size(), "Should return both active services under the salon");
        assertEquals("Gel Manicure Polish", salonServices.get(0).get("service_name"));
        assertEquals(
                new BigDecimal("180000"),
                new BigDecimal(salonServices.get(0).get("price").toString()));
        assertEquals(45, ((Number) salonServices.get(0).get("duration_minutes")).intValue());
        assertEquals("Acrylic Full Set", salonServices.get(1).get("service_name"));
        assertEquals(
                new BigDecimal("350000"),
                new BigDecimal(salonServices.get(1).get("price").toString()));
    }
}
