package com.ellora.api.characterization;

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
class ProfileUpdateIntegrationTest extends BaseCharacterizationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private TokenProvider tokens;

    @Test
    void updatesAuthenticatedUserAndRejectsAnonymousRequest() throws Exception {
        UUID id = insertUser(UUID.randomUUID() + "@example.com", "hash", "Before", "0900000000", "CUSTOMER");
        String token = tokens.generateAccessToken(id, "test@example.com", "CUSTOMER");
        String body = "{\"fullName\":\"After Name\",\"phone\":\"0912345678\"}";
        mockMvc.perform(put("/api/v1/auth/me").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("After Name"))
                .andExpect(jsonPath("$.phone").value("0912345678"));
        mockMvc.perform(put("/api/v1/auth/me").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is4xxClientError());
    }
}
