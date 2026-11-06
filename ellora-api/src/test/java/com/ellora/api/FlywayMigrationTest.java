package com.ellora.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verify Flyway migration executes and creates all schema tables")
    void shouldExecuteFlywayMigrationAndCreateAll21Tables() throws Exception {
        Set<String> expectedTables = Set.of(
                "users",
                "revoked_access_tokens",
                "salons",
                "service_categories",
                "nail_services",
                "employees",
                "working_schedules",
                "salon_working_hours",
                "salon_closures",
                "salon_profile_changes",
                "salon_subscription_plans",
                "salon_subscription_requests",
                "bookings",
                "booking_completion_disputes",
                "booking_emails",
                "reviews",
                "payments",
                "platform_billing_settings",
                "platform_fee_policies",
                "platform_statements",
                "platform_support_contacts",
                "refresh_tokens",
                "email_verification_tokens",
                "password_reset_tokens");

        Set<String> actualTables = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
                ResultSet rs = conn.getMetaData().getTables(null, "public", "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (!"flyway_schema_history".equalsIgnoreCase(tableName)) {
                    actualTables.add(tableName.toLowerCase());
                }
            }
        }

        assertEquals(24, actualTables.size(), "Should have exactly 24 tables created by Flyway migrations");
        assertTrue(actualTables.containsAll(expectedTables), "Should contain all 24 expected tables");
    }
}
