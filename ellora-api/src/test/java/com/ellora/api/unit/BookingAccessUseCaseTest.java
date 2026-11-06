package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.booking.BookingAccessUseCase;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.SalonRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingAccessUseCaseTest {
    @Mock BookingRepository bookings;
    @Mock SalonRepository salons;

    @Test
    void customerCanReadAndCancelOwnBookingButCannotConfirm() {
        UUID customer = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setCustomerId(customer);
        booking.setSalonId(5L);
        when(bookings.findById(10L)).thenReturn(Optional.of(booking));
        BookingAccessUseCase access = new BookingAccessUseCase(bookings, salons);
        assertThatCode(() -> access.check(10L, customer, false, null)).doesNotThrowAnyException();
        assertThatCode(() -> access.check(10L, customer, false, "CANCELLED")).doesNotThrowAnyException();
        assertThatThrownBy(() -> access.check(10L, customer, false, "CONFIRMED")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> access.check(10L, customer, false, "DISPUTED")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> access.check(10L, UUID.randomUUID(), false, null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void ownerCanConfirmOnlyForTheirSalon() {
        UUID owner = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setSalonId(5L);
        Salon salon = new Salon();
        salon.setOwnerId(owner);
        when(bookings.findById(10L)).thenReturn(Optional.of(booking));
        when(salons.findById(5L)).thenReturn(Optional.of(salon));
        BookingAccessUseCase access = new BookingAccessUseCase(bookings, salons);
        assertThatCode(() -> access.check(10L, owner, false, "CONFIRMED")).doesNotThrowAnyException();
        assertThatThrownBy(() -> access.check(10L, UUID.randomUUID(), false, "CONFIRMED")).isInstanceOf(BusinessRuleException.class);
    }
}
