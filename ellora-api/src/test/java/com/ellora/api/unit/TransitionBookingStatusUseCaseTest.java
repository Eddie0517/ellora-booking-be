package com.ellora.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.booking.TransitionBookingStatusUseCase;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.TransitionStatusRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransitionBookingStatusUseCaseTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TransitionBookingStatusUseCase transitionBookingStatusUseCase;

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = Booking.createNew(
                UUID.randomUUID(),
                1L,
                10L,
                null,
                Instant.now().plusSeconds(3600),
                45,
                new BigDecimal("150000"),
                "Note"
        );
        booking.setId(123L);
    }

    @Test
    @DisplayName("Should confirm pending booking")
    void shouldConfirmBooking() {
        when(bookingRepository.findById(123L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        TransitionStatusRequest request = new TransitionStatusRequest("CONFIRMED", null, null);
        BookingDetailsResponse response = transitionBookingStatusUseCase.execute(123L, request);

        assertEquals("CONFIRMED", response.status());
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("Should complete confirmed booking")
    void shouldCompleteBooking() {
        booking.confirm();
        when(bookingRepository.findById(123L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        TransitionStatusRequest request = new TransitionStatusRequest("COMPLETED", null, "Service was great");
        BookingDetailsResponse response = transitionBookingStatusUseCase.execute(123L, request);

        assertEquals("COMPLETED", response.status());
        assertNotNull(response.completedAt());
    }

    @Test
    @DisplayName("Should cancel booking with cancellation reason")
    void shouldCancelBooking() {
        when(bookingRepository.findById(123L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        TransitionStatusRequest request = new TransitionStatusRequest("CANCELLED", "Client reschedule needed", null);
        BookingDetailsResponse response = transitionBookingStatusUseCase.execute(123L, request);

        assertEquals("CANCELLED", response.status());
        assertEquals("Client reschedule needed", response.cancellationReason());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException on invalid status string")
    void shouldThrowOnInvalidStatus() {
        when(bookingRepository.findById(123L)).thenReturn(Optional.of(booking));

        TransitionStatusRequest request = new TransitionStatusRequest("UNKNOWN_STATUS", null, null);
        assertThrows(BusinessRuleException.class, () -> transitionBookingStatusUseCase.execute(123L, request));
    }
}
