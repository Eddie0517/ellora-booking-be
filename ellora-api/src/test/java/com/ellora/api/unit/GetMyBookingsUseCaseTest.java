package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.booking.GetMyBookingsUseCase;
import com.ellora.application.feature.booking.BookingResponseMapper;
import com.ellora.domain.model.Booking;
import com.ellora.domain.repository.BookingRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMyBookingsUseCaseTest {
    @Mock private BookingRepository bookings;

    @Test
    void returnsOnlyRequestedCustomerPage() {
        UUID customerId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(42L);
        booking.setStatus(com.ellora.domain.constant.BookingStatus.PENDING);
        when(bookings.findByCustomerId(customerId, 1, 10)).thenReturn(List.of(booking));
        when(bookings.countByCustomerId(customerId)).thenReturn  (12L);
        var mapper = new BookingResponseMapper(mock(com.ellora.domain.repository.UserRepository.class),
                mock(com.ellora.domain.repository.SalonRepository.class),
                mock(com.ellora.domain.repository.NailServiceRepository.class));
        var result = new GetMyBookingsUseCase(bookings, mapper).execute(customerId, 1, 10);
        assertThat(result.items()).extracting(item -> item.id()).containsExactly(42L);
        assertThat(result.totalItems()).isEqualTo(12);
        assertThat(result.totalPages()).isEqualTo(2);
    }
}
