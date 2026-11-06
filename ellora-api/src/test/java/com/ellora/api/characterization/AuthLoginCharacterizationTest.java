package com.ellora.api.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Characterization test for User Login flow (US5 - T037).
 * Verifies user password hash lookup and active/locked/disabled account states.
 */
class AuthLoginCharacterizationTest extends BaseCharacterizationTest {

    @Test
    @DisplayName("Should retrieve credentials and status for active user during login")
    void shouldRetrieveCredentialsForActiveUser() {
        String email = "login_active_" + UUID.randomUUID() + "@example.com";
        String passwordHash = "$argon2id$v=19$m=65536,t=3,p=4$activeuserhash";
        UUID userId = insertUserWithState(email, passwordHash, "Active User", "0911222333", "CUSTOMER", true, false);

        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT id, email, password_hash, role, enabled, locked FROM users WHERE email = ?", email);

        assertEquals(1, results.size());
        Map<String, Object> user = results.get(0);
        assertEquals(userId, user.get("id"));
        assertEquals(email, user.get("email"));
        assertEquals(passwordHash, user.get("password_hash"));
        assertEquals("CUSTOMER", user.get("role"));
        assertTrue((Boolean) user.get("enabled"));
        assertFalse((Boolean) user.get("locked"));
    }

    @Test
    @DisplayName("Should detect locked account status during login flow")
    void shouldDetectLockedAccount() {
        String email = "locked_" + UUID.randomUUID() + "@example.com";
        String passwordHash = "hash_locked";
        UUID userId = insertUserWithState(email, passwordHash, "Locked User", "0911222444", "CUSTOMER", true, true);

        Map<String, Object> user = jdbcTemplate.queryForMap("SELECT locked, enabled FROM users WHERE email = ?", email);

        assertTrue((Boolean) user.get("locked"), "Locked user must have locked=true");
        assertTrue((Boolean) user.get("enabled"));
    }

    @Test
    @DisplayName("Should detect disabled account status during login flow")
    void shouldDetectDisabledAccount() {
        String email = "disabled_" + UUID.randomUUID() + "@example.com";
        String passwordHash = "hash_disabled";
        UUID userId = insertUserWithState(email, passwordHash, "Disabled User", "0911222555", "CUSTOMER", false, false);

        Map<String, Object> user = jdbcTemplate.queryForMap("SELECT locked, enabled FROM users WHERE email = ?", email);

        assertFalse((Boolean) user.get("enabled"), "Disabled user must have enabled=false");
        assertFalse((Boolean) user.get("locked"));
    }

    @Test
    @DisplayName("Should return empty result when querying unknown email")
    void shouldReturnEmptyForUnknownEmail() {
        String unknownEmail = "nonexistent_" + UUID.randomUUID() + "@example.com";

        List<Map<String, Object>> results =
                jdbcTemplate.queryForList("SELECT id FROM users WHERE email = ?", unknownEmail);

        assertTrue(results.isEmpty(), "Unknown email query must return empty list");
    }
}
