package com.ellora.api.characterization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class WorkingHoursIntegrationTest extends BaseCharacterizationTest {
    @Autowired MockMvc mvc;
    @Autowired TokenProvider tokens;

    @Test
    void ownerSavesAndPublicReadsSchedule() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        Long salon = insertSalon(owner, "Salon", "Address", "City", "ACTIVE");
        String token = tokens.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        String body = "[" + java.util.Arrays.stream(java.time.DayOfWeek.values())
                .map(day -> "{\"dayOfWeek\":\"" + day + "\",\"openTime\":\"09:00\",\"closeTime\":\"18:00\",\"closed\":false}")
                .collect(java.util.stream.Collectors.joining(",")) + "]";
        mvc.perform(put("/api/v1/owner/salon/working-hours").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].openTime").value("09:00:00"));
        mvc.perform(get("/api/v1/salons/" + salon + "/working-hours"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].closed").value(false));
        mvc.perform(put("/api/v1/owner/salon/working-hours").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is4xxClientError());
    }
}
