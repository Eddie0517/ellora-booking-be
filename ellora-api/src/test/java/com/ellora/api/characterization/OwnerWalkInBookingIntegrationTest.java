package com.ellora.api.characterization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.common.port.TokenProvider;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class OwnerWalkInBookingIntegrationTest extends BaseCharacterizationTest {
    @Autowired MockMvc mvc;
    @Autowired TokenProvider tokens;

    @Test
    void ownerCanCreateWalkInBookingForTheirSalonAndEndpointRequiresAuthentication() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        Long salon = insertSalon(owner, "Salon", "Address", "City", "ACTIVE");
        Long category = insertCategory("Nail care");
        Long service = insertService(salon, category, "Manicure", new BigDecimal("250000"), 60);
        Long employee = insertEmployee(salon, "Technician", "0912345678");
        String token = tokens.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        String payload = "{\"serviceId\":" + service + ",\"employeeId\":" + employee
                + ",\"scheduledAt\":\"2027-01-01T10:00:00Z\",\"customerName\":\"Walk In\","
                + "\"customerPhone\":\"0911222333\",\"customerNote\":\"At salon\"}";

        mvc.perform(post("/api/v1/owner/bookings/walk-ins")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/owner/bookings/walk-ins").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.salonId").value(salon))
                .andExpect(jsonPath("$.serviceId").value(service))
                .andExpect(jsonPath("$.walkInName").value("Walk In"))
                .andExpect(jsonPath("$.walkInPhone").value("0911222333"));
    }
}
