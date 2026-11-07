package com.ellora.api.characterization;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base test harness for Legacy Characterization Test Suite.
 * Connects against running PostgreSQL container with baseline schema.
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public abstract class BaseCharacterizationTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    protected jakarta.persistence.EntityManager entityManager;

    protected void clearPersistenceContext() {
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    protected UUID insertUser(String email, String passwordHash, String fullName, String phone, String role) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, full_name, phone, role, enabled, locked) "
                        + "VALUES (?, ?, ?, ?, ?, ?, true, false)",
                id,
                email,
                passwordHash,
                fullName,
                phone,
                role);
        return id;
    }

    protected UUID insertUserWithState(
            String email,
            String passwordHash,
            String fullName,
            String phone,
            String role,
            boolean enabled,
            boolean locked) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, full_name, phone, role, enabled, locked) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                email,
                passwordHash,
                fullName,
                phone,
                role,
                enabled,
                locked);
        return id;
    }

    protected Long insertSalon(UUID ownerId, String name, String address, String city, String status) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO salons (owner_id, name, address, city, status) " + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                ownerId,
                name,
                address,
                city,
                status);
    }

    protected Long insertCategory(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO service_categories (name) VALUES (?) RETURNING id", Long.class, name);
    }

    protected Long insertService(Long salonId, Long categoryId, String name, BigDecimal price, int durationMinutes) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO nail_services (salon_id, category_id, name, price, duration_minutes, active) "
                        + "VALUES (?, ?, ?, ?, ?, true) RETURNING id",
                Long.class,
                salonId,
                categoryId,
                name,
                price,
                durationMinutes);
    }

    protected Long insertEmployee(Long salonId, String fullName, String phone) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO employees (salon_id, full_name, phone, active) " + "VALUES (?, ?, ?, true) RETURNING id",
                Long.class,
                salonId,
                fullName,
                phone);
    }

    protected Long insertBooking(
            UUID customerId,
            Long salonId,
            Long serviceId,
            Long employeeId,
            OffsetDateTime scheduledAt,
            int durationMinutes,
            BigDecimal servicePriceSnapshot,
            String status) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO bookings (customer_id, salon_id, service_id, employee_id, scheduled_at, duration_minutes, "
                        + "service_price_snapshot, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                customerId,
                salonId,
                serviceId,
                employeeId,
                scheduledAt,
                durationMinutes,
                servicePriceSnapshot,
                status);
    }

    protected Long insertGuestBooking(
            String walkInName,
            String walkInPhone,
            String guestEmail,
            Long salonId,
            Long serviceId,
            Long employeeId,
            OffsetDateTime scheduledAt,
            int durationMinutes,
            BigDecimal servicePriceSnapshot,
            String status) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO bookings (walk_in_name, walk_in_phone, guest_email, salon_id, service_id, employee_id, "
                        + "scheduled_at, duration_minutes, service_price_snapshot, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                walkInName,
                walkInPhone,
                guestEmail,
                salonId,
                serviceId,
                employeeId,
                scheduledAt,
                durationMinutes,
                servicePriceSnapshot,
                status);
    }
}
