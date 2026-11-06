package com.ellora.api.characterization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.common.port.TokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class SalonClosuresIntegrationTest extends BaseCharacterizationTest {
    @Autowired MockMvc mvc;
    @Autowired TokenProvider tokens;

    @Test
    void ownerManagesClosuresAndPublicCanReadDates() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        Long salon = insertSalon(owner, "Salon", "Address", "City", "ACTIVE");
        String token = tokens.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        String payload = "{\"date\":\"2027-01-01\",\"reason\":\"Nghỉ lễ\"}";

        mvc.perform(post("/api/v1/owner/salon/closures").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.date").value("2027-01-01"));
        mvc.perform(get("/api/v1/owner/salon/closures").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].reason").value("Nghỉ lễ"));
        mvc.perform(get("/api/v1/salons/" + salon + "/closures?from=2027-01-01&to=2027-01-31"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0]").value("2027-01-01"));
        Long closureId = jdbcTemplate.queryForObject("SELECT id FROM salon_closures WHERE salon_id = ?", Long.class, salon);
        mvc.perform(delete("/api/v1/owner/salon/closures/" + closureId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/salons/" + salon + "/closures?from=2027-01-01&to=2027-01-31"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }
}
