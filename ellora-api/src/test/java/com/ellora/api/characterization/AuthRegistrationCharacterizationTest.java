package com.ellora.api.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Characterization test for User Registration flow (US5 - T036).
 * Verifies user creation, email uniqueness, role assignment, and default state in `users` table.
 */
class AuthRegistrationCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should create customer user with defaults: enabled=true, locked=false, timestamps populated")
    void shouldCreateCustomerUserWithDefaults() {
        String email = "customer_" + UUID.randomUUID() + "@example.com";
        String passwordHash = "$argon2id$v=19$m=65536,t=3,p=4$dummyhash";
        String fullName = "Nguyen Van A";
        String phone = "0901234567";

        UUID userId = insertUser(email, passwordHash, fullName, phone, "CUSTOMER");
        assertNotNull(userId);

        Map<String, Object> user = jdbcTemplate.queryForMap(
                "SELECT id, email, password_hash, full_name, phone, role, enabled, locked, created_at, updated_at "
                        + "FROM users WHERE id = ?",
                userId);

        assertEquals(email, user.get("email"));
        assertEquals(passwordHash, user.get("password_hash"));
        assertEquals(fullName, user.get("full_name"));
        assertEquals(phone, user.get("phone"));
        assertEquals("CUSTOMER", user.get("role"));
        assertEquals(Boolean.TRUE, user.get("enabled"));
        assertEquals(Boolean.FALSE, user.get("locked"));
        assertNotNull(user.get("created_at"));
        assertNotNull(user.get("updated_at"));
    }

    @Test
    @DisplayName("Should enforce email uniqueness constraint")
    void shouldEnforceEmailUniqueness() {
        String email = "duplicate_" + UUID.randomUUID() + "@example.com";
        insertUser(email, "hash1", "User 1", "0900000001", "CUSTOMER");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertUser(email, "hash2", "User 2", "0900000002", "CUSTOMER"),
                "Inserting a duplicate email must violate unique constraint idx_users_email");
    }

    @Test
    @DisplayName("Should persist valid user roles: CUSTOMER, SALON_OWNER, STAFF, ADMIN")
    void shouldSupportAllSystemRoles() {
        String[] roles = {"CUSTOMER", "SALON_OWNER", "STAFF", "ADMIN"};

        for (String role : roles) {
            String email = role.toLowerCase() + "_" + UUID.randomUUID() + "@example.com";
            UUID id = insertUser(email, "hash", "Name " + role, "0900000000", role);

            String storedRole = jdbcTemplate.queryForObject("SELECT role FROM users WHERE id = ?", String.class, id);
            assertEquals(role, storedRole);
        }
    }

    @Test
    @DisplayName("Should update email_verified_at when email verification occurs")
    void shouldUpdateEmailVerifiedAt() {
        String email = "unverified_" + UUID.randomUUID() + "@example.com";
        UUID id = insertUser(email, "hash", "Unverified User", "0900000000", "CUSTOMER");

        int updated = jdbcTemplate.update("UPDATE users SET email_verified_at = CURRENT_TIMESTAMP WHERE id = ?", id);
        assertEquals(1, updated);

        Map<String, Object> user = jdbcTemplate.queryForMap("SELECT email_verified_at FROM users WHERE id = ?", id);
        assertNotNull(user.get("email_verified_at"));
    }
}
