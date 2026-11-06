package com.ellora.api.characterization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.common.port.PaymentGateway;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@TestPropertySource(properties = "app.payos.checksum-key=test-checksum-key")
class SalonSubscriptionIntegrationTest extends BaseCharacterizationTest {
    @Autowired MockMvc mvc;
    @Autowired TokenProvider tokens;
    @MockBean PaymentGateway paymentGateway;
    @Test
    void ownerAndAdminCanSyncOnlyMatchingPaidPayosOrder() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000002", "SALON_OWNER");
        UUID admin = insertUser(UUID.randomUUID() + "@example.com", "hash", "Admin", "0900000003", "ADMIN");
        Long salon = insertSalon(owner, "PayOS Salon", "Address", "City", "ACTIVE");
        jdbcTemplate.update("UPDATE salon_subscription_plans SET monthly_price=99000, enabled=true WHERE tier='VIP'");
        String ownerToken = tokens.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        String adminToken = tokens.generateAccessToken(admin, "admin@example.com", "ADMIN");
        mvc.perform(post("/api/v1/owner/subscription/requests").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tier\":\"VIP\",\"expectedPrice\":99000}"))
                .andExpect(status().isCreated());
        Long requestId = jdbcTemplate.queryForObject("SELECT id FROM salon_subscription_requests WHERE salon_id=?", Long.class, salon);
        when(paymentGateway.createSubscriptionLink(anyLong(), anyLong(), anyString()))
                .thenReturn(new PaymentGateway.PaymentLink("sync-link", "https://pay.payos.vn/web/sync-link", "qr-data"));
        mvc.perform(post("/api/v1/owner/subscription/requests/" + requestId + "/prepare-payment")
                        .header("Authorization", "Bearer " + ownerToken)).andExpect(status().isOk());
        Long orderCode = jdbcTemplate.queryForObject("SELECT payos_order_code FROM salon_subscription_requests WHERE id=?", Long.class, requestId);
        when(paymentGateway.getSubscriptionPayment(orderCode))
                .thenReturn(new PaymentGateway.PaymentStatus(orderCode, "sync-link", 99000, 0, "PENDING", null));
        mvc.perform(post("/api/v1/owner/subscription/requests/" + requestId + "/sync-payment")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.paymentReportedAt").doesNotExist());
        when(paymentGateway.getSubscriptionPayment(orderCode))
                .thenReturn(new PaymentGateway.PaymentStatus(orderCode, "sync-link", 99000, 0, "CANCELLED", null));
        mvc.perform(post("/api/v1/owner/subscription/requests/" + requestId + "/sync-payment")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payosCheckoutUrl").doesNotExist());
        mvc.perform(post("/api/v1/owner/subscription/requests/" + requestId + "/prepare-payment")
                        .header("Authorization", "Bearer " + ownerToken)).andExpect(status().isOk());
        orderCode = jdbcTemplate.queryForObject("SELECT payos_order_code FROM salon_subscription_requests WHERE id=?", Long.class, requestId);
        when(paymentGateway.getSubscriptionPayment(orderCode))
                .thenReturn(new PaymentGateway.PaymentStatus(orderCode, "sync-link", 99000, 1000, "PAID", null));
        mvc.perform(post("/api/v1/admin/subscription-requests/" + requestId + "/sync-payment")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isUnprocessableEntity());
        when(paymentGateway.getSubscriptionPayment(orderCode))
                .thenReturn(new PaymentGateway.PaymentStatus(orderCode, "sync-link", 99000, 99000, "PAID", "SYNC-REF"));
        mvc.perform(post("/api/v1/admin/subscription-requests/" + requestId + "/sync-payment")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.receivedAmount").value(99000))
                .andExpect(jsonPath("$.paymentReference").value("SYNC-REF"));
    }

    @Test
    void ownerPaysWithVerifiedWebhookBeforeAdminCanActivateSubscription() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        UUID admin = insertUser(UUID.randomUUID() + "@example.com", "hash", "Admin", "0900000001", "ADMIN");
        Long salon = insertSalon(owner, "Salon", "Address", "City", "ACTIVE");
        String ownerToken = tokens.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        String adminToken = tokens.generateAccessToken(admin, "admin@example.com", "ADMIN");

        mvc.perform(put("/api/v1/admin/subscription-plans/VIP").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monthlyPrice\":99000,\"enabled\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tier").value("VIP"));
        mvc.perform(get("/api/v1/salon-plans"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.tier == 'VIP')].monthlyPrice").value(99000));
        mvc.perform(get("/api/v1/owner/subscription").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.salonId").value(salon));

        mvc.perform(post("/api/v1/owner/subscription/requests").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tier\":\"VIP\",\"expectedPrice\":99000}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.quotedPrice").value(99000));
        Long requestId = jdbcTemplate.queryForObject("SELECT id FROM salon_subscription_requests WHERE salon_id = ?", Long.class, salon);
        when(paymentGateway.createSubscriptionLink(anyLong(), anyLong(), anyString()))
                .thenReturn(new PaymentGateway.PaymentLink("link-1", "https://pay.payos.vn/web/link-1", "qr-data"));

        mvc.perform(post("/api/v1/owner/subscription/requests/" + requestId + "/prepare-payment")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payosCheckoutUrl").value("https://pay.payos.vn/web/link-1"));
        Long orderCode = jdbcTemplate.queryForObject("SELECT payos_order_code FROM salon_subscription_requests WHERE id=?", Long.class, requestId);
        String paymentData = "{\"orderCode\":" + orderCode + ",\"amount\":99000,\"paymentLinkId\":\"link-1\",\"reference\":\"BANK-REF-1\",\"code\":\"00\"}";
        String canonical = "amount=99000&code=00&orderCode=" + orderCode + "&paymentLinkId=link-1&reference=BANK-REF-1";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("test-checksum-key".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        mvc.perform(patch("/api/v1/admin/subscription-requests/" + requestId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/v1/webhooks/payos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"00\",\"success\":true,\"data\":" + paymentData + ",\"signature\":\"bad\"}"))
                .andExpect(status().isUnprocessableEntity());
        String webhook = "{\"code\":\"00\",\"success\":true,\"data\":" + paymentData + ",\"signature\":\"" + signature + "\"}";
        mvc.perform(post("/api/v1/webhooks/payos").contentType(MediaType.APPLICATION_JSON).content(webhook))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/admin/subscription-requests/" + requestId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"REJECTED\",\"note\":\"Không phù hợp\"}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/v1/webhooks/payos").contentType(MediaType.APPLICATION_JSON).content(webhook))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/subscription-requests?status=PENDING").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].salonId").value(salon));
        mvc.perform(patch("/api/v1/admin/subscription-requests/" + requestId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"note\":\"Đã đối soát\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.receivedAmount").value(99000));
        mvc.perform(get("/api/v1/owner/subscription").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.effectiveTier").value("VIP"));
        mvc.perform(get("/api/v1/salons/" + salon))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subscriptionTier").value("VIP"));
    }
}
