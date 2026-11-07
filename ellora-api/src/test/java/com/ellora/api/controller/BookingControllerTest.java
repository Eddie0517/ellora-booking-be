package com.ellora.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.feature.booking.CreateBookingUseCase;
import com.ellora.application.feature.booking.GetBookingDetailsUseCase;
import com.ellora.application.feature.booking.BookingAccessUseCase;
import com.ellora.application.feature.booking.TransitionBookingStatusUseCase;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.CreateBookingRequest;
import com.ellora.application.feature.booking.dto.TransitionStatusRequest;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.domain.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@WithMockUser(roles = "CUSTOMER")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @MockBean
    private CreateBookingUseCase createBookingUseCase;

    @MockBean
    private GetBookingDetailsUseCase getBookingDetailsUseCase;

    @MockBean
    private BookingAccessUseCase bookingAccessUseCase;

    @MockBean
    private TransitionBookingStatusUseCase transitionBookingStatusUseCase;

    @Test
    @DisplayName("POST /api/v1/bookings should create an authenticated customer's booking")
    void shouldCreateCustomerBooking() throws Exception {
        UUID customerId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(customerId, "customer@example.com", "CUSTOMER");
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateBookingRequest request = new CreateBookingRequest(
                1L, 10L, null, scheduledAt, 45, "Customer note", null, null, null);

        BookingDetailsResponse response = new BookingDetailsResponse(
                100L, customerId, "Customer Name", "customer@example.com", "0912345678", 1L, 10L, null,
                scheduledAt, 45, new BigDecimal("200000"), "PENDING", "Customer note", null, null, null, Instant.now(), Instant.now());

        when(createBookingUseCase.execute(eq(customerId), any(CreateBookingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.servicePriceSnapshot").value(200000));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/v1/bookings should reject unauthenticated requests")
    void shouldRejectUnauthenticatedBooking() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/bookings should reject non-customer roles")
    void shouldRejectOwnerBookingAsCustomer() throws Exception {
        String ownerToken = tokenProvider.generateAccessToken(UUID.randomUUID(), "owner@example.com", "SALON_OWNER");
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/bookings should validate missing required fields and return 400")
    void shouldReturnBadRequestOnInvalidCreatePayload() throws Exception {
        String invalidPayload = "{}";

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/validation-error"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} should return booking details with 200 OK")
    void shouldGetBookingDetails() throws Exception {
        Instant scheduledAt = Instant.now().plus(2, ChronoUnit.DAYS);
        BookingDetailsResponse response = new BookingDetailsResponse(
                55L, UUID.randomUUID(), null, null, null, 2L, 20L, 5L,
                scheduledAt, 60, new BigDecimal("350000"), "CONFIRMED", null, null, null, null, Instant.now(), Instant.now());

        when(getBookingDetailsUseCase.execute(55L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/bookings/55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.salonId").value(2))
                .andExpect(jsonPath("$.serviceId").value(20))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} should return 404 when booking not found")
    void shouldReturn404WhenBookingNotFound() throws Exception {
        when(getBookingDetailsUseCase.execute(999L))
                .thenThrow(new BusinessRuleException("BOOKING_NOT_FOUND", "Không tìm thấy lịch hẹn"));

        mockMvc.perform(get("/api/v1/bookings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/business-rule-violation"))
                .andExpect(jsonPath("$.detail").value("Không tìm thấy lịch hẹn"));
    }

    @Test
    @DisplayName("PUT /api/v1/bookings/{id}/status should transition status and return 200 OK")
    void shouldTransitionBookingStatus() throws Exception {
        TransitionStatusRequest request = new TransitionStatusRequest("CONFIRMED", null, null);
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);

        BookingDetailsResponse response = new BookingDetailsResponse(
                55L, UUID.randomUUID(), null, null, null, 2L, 20L, null,
                scheduledAt, 30, new BigDecimal("150000"), "CONFIRMED", null, null, null, null, Instant.now(), Instant.now());

        when(transitionBookingStatusUseCase.execute(eq(55L), any(TransitionStatusRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/bookings/55/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/v1/bookings/{id}/status should return 422 on invalid status transition")
    void shouldReturn422OnInvalidStatusTransition() throws Exception {
        TransitionStatusRequest request = new TransitionStatusRequest("COMPLETED", null, null);

        when(transitionBookingStatusUseCase.execute(eq(55L), any(TransitionStatusRequest.class)))
                .thenThrow(new BusinessRuleException("INVALID_STATUS_TRANSITION", "Không thể hoàn thành lịch hẹn ở trạng thái PENDING"));

        mockMvc.perform(put("/api/v1/bookings/55/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/business-rule-violation"));
    }
}
