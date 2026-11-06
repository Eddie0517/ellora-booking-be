package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.booking.HideCancelledBookingUseCase;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HideCancelledBookingUseCaseTest {
    @Mock private BookingRepository bookings;

    @Test
    void hidesOnlyTheCustomersCancelledBooking() {
        UUID customerId = UUID.randomUUID();
        Booking booking = booking(customerId, BookingStatus.CANCELLED);
        when(bookings.findById(42L)).thenReturn(Optional.of(booking));

        new HideCancelledBookingUseCase(bookings).execute(42L, customerId);

        verify(bookings).hideCancelledForCustomer(42L, customerId);
    }

    @Test
    void rejectsAnotherCustomersBooking() {
        UUID customerId = UUID.randomUUID();
        when(bookings.findById(42L)).thenReturn(Optional.of(booking(UUID.randomUUID(), BookingStatus.CANCELLED)));

        assertThatThrownBy(() -> new HideCancelledBookingUseCase(bookings).execute(42L, customerId))
                .isInstanceOf(BusinessRuleException.class);
        verify(bookings, never()).hideCancelledForCustomer(42L, customerId);
    }

    @Test
    void rejectsActiveBooking() {
        UUID customerId = UUID.randomUUID();
        when(bookings.findById(42L)).thenReturn(Optional.of(booking(customerId, BookingStatus.CONFIRMED)));

        assertThatThrownBy(() -> new HideCancelledBookingUseCase(bookings).execute(42L, customerId))
                .isInstanceOf(BusinessRuleException.class);
        verify(bookings, never()).hideCancelledForCustomer(42L, customerId);
    }

    private Booking booking(UUID customerId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(42L);
        booking.setCustomerId(customerId);
        booking.setStatus(status);
        return booking;
    }
}
