package com.ellora.api.characterization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.application.common.port.TokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AdminBillingQrIntegrationTest extends BaseCharacterizationTest {
    @Autowired MockMvc mvc;
    @Autowired TokenProvider tokens;
    @MockBean ImageStorage imageStorage;

    @Test
    void adminCanConfigurePaymentQrAndOwnerCannot() throws Exception {
        UUID admin = insertUser(UUID.randomUUID() + "@example.com", "hash", "Admin", "0900000001", "ADMIN");
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        String adminToken = tokens.generateAccessToken(admin, "admin@example.com", "ADMIN");
        String ownerToken = tokens.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        String qrUrl = "https://example.test/platform-payment-qr.png";
        when(imageStorage.uploadImage(eq("platform/billing"), eq("payment-qr.png"), any(), eq(4L), eq("image/png")))
                .thenReturn(qrUrl);
        MockMultipartFile file = new MockMultipartFile("file", "payment-qr.png", "image/png", new byte[] {1, 2, 3, 4});

        mvc.perform(multipart("/api/v1/admin/billing/qr").file(file).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/v1/admin/billing/qr").file(file).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.url").value(qrUrl));
        mvc.perform(get("/api/v1/admin/billing/qr").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.url").value(qrUrl));
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT qr_url FROM platform_billing_settings WHERE id = 1", String.class)).isEqualTo(qrUrl);
    }
}
