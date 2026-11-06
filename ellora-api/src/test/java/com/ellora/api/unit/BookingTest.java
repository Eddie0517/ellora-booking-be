package com.ellora.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookingTest {

    @Test
    @DisplayName("createNew should create booking with PENDING status and valid fields")
    void shouldCreateNewRegisteredCustomerBooking() {
        UUID customerId = UUID.randomUUID();
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);

        Booking booking = Booking.createNew(
                customerId,
                10L,
                20L,
                30L,
                scheduledAt,
                60,
                new BigDecimal("250000"),
                "Please do nail art"
        );

        assertNotNull(booking);
        assertEquals(customerId, booking.getCustomerId());
        assertEquals(10L, booking.getSalonId());
        assertEquals(20L, booking.getServiceId());
        assertEquals(30L, booking.getEmployeeId());
        assertEquals(scheduledAt, booking.getScheduledAt());
        assertEquals(60, booking.getDurationMinutes());
        assertEquals(new BigDecimal("250000"), booking.getServicePriceSnapshot());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals("Please do nail art", booking.getCustomerNote());
        assertNotNull(booking.getCreatedAt());
        assertNotNull(booking.getUpdatedAt());
    }

    @Test
    @DisplayName("createNew should reject null customerId, salonId, serviceId, scheduledAt, or invalid duration")
    void shouldValidateInvariantsOnCreateNew() {
        UUID customerId = UUID.randomUUID();
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);

        assertThrows(BusinessRuleException.class, () ->
                Booking.createNew(null, 10L, 20L, null, scheduledAt, 30, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createNew(customerId, null, 20L, null, scheduledAt, 30, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createNew(customerId, 10L, null, null, scheduledAt, 30, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createNew(customerId, 10L, 20L, null, null, 30, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createNew(customerId, 10L, 20L, null, scheduledAt, 0, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createNew(customerId, 10L, 20L, null, scheduledAt, 30, new BigDecimal("-1"), null));
    }

    @Test
    @DisplayName("createGuest should require guest name and at least one contact channel")
    void shouldValidateInvariantsOnCreateGuest() {
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);

        assertThrows(BusinessRuleException.class, () ->
                Booking.createGuest(null, "guest@test.com", "0901234567", 1L, 2L, null, scheduledAt, 30, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createGuest("   ", "guest@test.com", "0901234567", 1L, 2L, null, scheduledAt, 30, BigDecimal.TEN, null));

        assertThrows(BusinessRuleException.class, () ->
                Booking.createGuest("Walk-in", null, null, 1L, 2L, null, scheduledAt, 30, BigDecimal.TEN, null));

        Booking guestBooking = Booking.createGuest("Guest A", "guest@a.com", null, 1L, 2L, null, scheduledAt, 45, new BigDecimal("150000"), null);
        assertNull(guestBooking.getCustomerId());
        assertEquals("Guest A", guestBooking.getWalkInName());
        assertEquals("guest@a.com", guestBooking.getGuestEmail());
        assertEquals(BookingStatus.PENDING, guestBooking.getStatus());
    }

    @Test
    @DisplayName("confirm should transition from PENDING to CONFIRMED")
    void shouldTransitionToConfirmed() {
        Booking booking = Booking.createNew(
                UUID.randomUUID(), 1L, 2L, null, Instant.now().plus(1, ChronoUnit.DAYS), 30, BigDecimal.TEN, null);

        booking.confirm();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // Confirming again must fail
        assertThrows(BusinessRuleException.class, booking::confirm);
    }

    @Test
    @DisplayName("complete should transition to COMPLETED and record completedAt and confirmation note")
    void shouldTransitionToCompleted() {
        Booking booking = Booking.createNew(
                UUID.randomUUID(), 1L, 2L, null, Instant.now().plus(1, ChronoUnit.DAYS), 30, BigDecimal.TEN, null);

        booking.confirm();
        booking.complete("CUSTOMER_CONFIRMED");

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        assertNotNull(booking.getCompletedAt());
        assertEquals("CUSTOMER_CONFIRMED", booking.getCompletionConfirmation());

        // Completing already completed booking must fail
        assertThrows(BusinessRuleException.class, booking::complete);
    }

    @Test
    @DisplayName("cancel should transition to CANCELLED and record reason")
    void shouldTransitionToCancelled() {
        Booking booking = Booking.createNew(
                UUID.randomUUID(), 1L, 2L, null, Instant.now().plus(1, ChronoUnit.DAYS), 30, BigDecimal.TEN, null);

        booking.cancel("Customer requested cancellation");
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("Customer requested cancellation", booking.getCancellationReason());

        // Cancelling again must fail
        assertThrows(BusinessRuleException.class, () -> booking.cancel("Another reason"));
    }

    @Test
    @DisplayName("dispute should only be allowed on COMPLETED bookings")
    void shouldTransitionToDisputed() {
        Booking booking = Booking.createNew(
                UUID.randomUUID(), 1L, 2L, null, Instant.now().plus(1, ChronoUnit.DAYS), 30, BigDecimal.TEN, null);

        // Disputing PENDING booking must fail
        assertThrows(BusinessRuleException.class, () -> booking.dispute("Unsatisfactory"));

        booking.confirm();
        booking.complete();
        booking.dispute("Unsatisfactory nail shape");

        assertEquals(BookingStatus.DISPUTED, booking.getStatus());
        assertEquals("Unsatisfactory nail shape", booking.getCompletionCustomerNote());
    }
}
