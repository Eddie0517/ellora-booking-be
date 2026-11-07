package com.ellora.api.characterization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;

import com.ellora.application.common.port.TokenProvider;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class MyBookingsIntegrationTest extends BaseCharacterizationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private TokenProvider tokenProvider;

    @Test
    void listsOnlyAuthenticatedCustomersBookings() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        UUID customer = insertUser(UUID.randomUUID() + "@example.com", "hash", "Customer", "0900000001", "CUSTOMER");
        UUID other = insertUser(UUID.randomUUID() + "@example.com", "hash", "Other", "0900000002", "CUSTOMER");
        Long salon = insertSalon(owner, "Salon", "Address", "City", "ACTIVE");
        Long service = insertService(salon, null, "Service", BigDecimal.TEN, 30);
        Long booking = insertBooking(customer, salon, service, null, OffsetDateTime.now().plusDays(1), 30, BigDecimal.TEN, "PENDING");
        jdbcTemplate.update("UPDATE bookings SET customer_phone = ? WHERE id = ?", "0988111222", booking);
        Long otherBooking = insertBooking(other, salon, service, null, OffsetDateTime.now().plusDays(2), 30, BigDecimal.TEN, "PENDING");
        String token = tokenProvider.generateAccessToken(customer, "customer@example.com", "CUSTOMER");
        mockMvc.perform(get("/api/v1/bookings/my").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].id").value(booking))
                .andExpect(jsonPath("$.items[0].customerName").value("Customer"))
                .andExpect(jsonPath("$.items[0].customerPhone").value("0988111222"))
                .andExpect(jsonPath("$.items[0].salonName").value("Salon"))
                .andExpect(jsonPath("$.items[0].serviceName").value("Service"));
        mockMvc.perform(get("/api/v1/bookings/my")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/v1/bookings/" + booking).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(booking));
        mockMvc.perform(get("/api/v1/bookings/" + otherBooking).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/bookings/" + otherBooking + "/status")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\"}")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/bookings/" + booking)).andExpect(status().is4xxClientError());
        String ownerToken = tokenProvider.generateAccessToken(owner, "owner@example.com", "SALON_OWNER");
        mockMvc.perform(get("/api/v1/bookings/" + otherBooking).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        jdbcTemplate.update("UPDATE bookings SET status='COMPLETED', completed_at=NOW(), "
                + "completion_confirmation='AWAITING_CUSTOMER', completion_version=1 WHERE id=?", booking);
        clearPersistenceContext();
        mockMvc.perform(post("/api/v1/bookings/" + booking + "/completion-response")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":false,\"completionVersion\":1,\"reason\":\"Chưa hoàn tất\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPUTED"))
                .andExpect(jsonPath("$.completionConfirmation").value("DISPUTED"));
        mockMvc.perform(post("/api/v1/bookings/" + booking + "/completion-response")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":false,\"completionVersion\":1,\"reason\":\"Lặp lại\"}"))
                .andExpect(status().isUnprocessableEntity());
        UUID admin = insertUser(UUID.randomUUID() + "@example.com", "hash", "Admin", "0900000009", "ADMIN");
        String adminToken = tokenProvider.generateAccessToken(admin, "admin@example.com", "ADMIN");
        mockMvc.perform(get("/api/v1/admin/booking-disputes?status=OPEN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.bookingId == " + booking + ")].reason").value(hasItem("Chưa hoàn tất")));
        mockMvc.perform(patch("/api/v1/admin/booking-disputes/" + booking + "/resolution")
                        .header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outcome\":\"REOPENED\",\"note\":\"Salon thực hiện lại\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REOPENED"));
        mockMvc.perform(get("/api/v1/bookings/" + booking).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
