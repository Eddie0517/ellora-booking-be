package com.ellora.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.booking.CreateBookingUseCase;
import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.dto.CreateBookingRequest;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
class CreateBookingUseCaseTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private NailServiceRepository nailServiceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CreateBookingUseCase createBookingUseCase;

    private Salon mockSalon;
    private NailService mockService;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockSalon = new Salon();
        mockSalon.setId(1L);
        mockSalon.setName("Beauty Lounge");
        mockSalon.setStatus(SalonStatus.ACTIVE);

        mockService = new NailService();
        mockService.setId(10L);
        mockService.setSalonId(1L);
        mockService.setName("Gel Manicure");
        mockService.setPrice(new BigDecimal("200000"));
        mockService.setDurationMinutes(45);

        mockEmployee = new Employee();
        mockEmployee.setId(100L);
        mockEmployee.setSalonId(1L);
        mockEmployee.setFullName("Linh Nail");
    }

    @Test
    @DisplayName("Should create booking for authenticated customer with price snapshot")
    void shouldCreateBookingForCustomer() {
        UUID customerId = UUID.randomUUID();
        Instant scheduledAt = Instant.now().plus(2, ChronoUnit.DAYS);
        CreateBookingRequest request = new CreateBookingRequest(
                1L, 10L, 100L, scheduledAt, null, "First time customer", null, null, null);

        when(salonRepository.findById(1L)).thenReturn(Optional.of(mockSalon));
        when(nailServiceRepository.findById(10L)).thenReturn(Optional.of(mockService));
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(mockEmployee));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(555L);
            return b;
        });

        BookingDetailsResponse response = createBookingUseCase.execute(customerId, request);

        assertNotNull(response);
        assertEquals(555L, response.id());
        assertEquals(customerId, response.customerId());
        assertEquals(1L, response.salonId());
        assertEquals(10L, response.serviceId());
        assertEquals(100L, response.employeeId());
        assertEquals(45, response.durationMinutes());
        assertEquals(new BigDecimal("200000"), response.servicePriceSnapshot());
        assertEquals("PENDING", response.status());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should create booking for guest customer with walk-in contact info")
    void shouldCreateBookingForGuest() {
        Instant scheduledAt = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateBookingRequest request = new CreateBookingRequest(
                1L, 10L, null, scheduledAt, 60, null, "Guest Name", "guest@example.com", "0912345678");

        when(salonRepository.findById(1L)).thenReturn(Optional.of(mockSalon));
        when(nailServiceRepository.findById(10L)).thenReturn(Optional.of(mockService));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(777L);
            return b;
        });

        BookingDetailsResponse response = createBookingUseCase.execute(null, request);

        assertNotNull(response);
        assertEquals(777L, response.id());
        assertNull(response.customerId());
        assertEquals("Guest Name", response.walkInName());
        assertEquals("guest@example.com", response.guestEmail());
        assertEquals("0912345678", response.walkInPhone());
        assertEquals(60, response.durationMinutes());
        assertEquals(new BigDecimal("200000"), response.servicePriceSnapshot());
    }

    @Test
    @DisplayName("Should fail when salon does not exist")
    void shouldFailWhenSalonNotFound() {
        CreateBookingRequest request = new CreateBookingRequest(
                999L, 10L, null, Instant.now().plus(1, ChronoUnit.DAYS), null, null, null, null, null);

        when(salonRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> createBookingUseCase.execute(UUID.randomUUID(), request));
    }

    @Test
    @DisplayName("Should fail when service does not belong to the salon")
    void shouldFailWhenServiceNotInSalon() {
        NailService wrongSalonService = new NailService();
        wrongSalonService.setId(20L);
        wrongSalonService.setSalonId(999L);

        CreateBookingRequest request = new CreateBookingRequest(
                1L, 20L, null, Instant.now().plus(1, ChronoUnit.DAYS), null, null, null, null, null);

        when(salonRepository.findById(1L)).thenReturn(Optional.of(mockSalon));
        when(nailServiceRepository.findById(20L)).thenReturn(Optional.of(wrongSalonService));

        assertThrows(BusinessRuleException.class, () -> createBookingUseCase.execute(UUID.randomUUID(), request));
    }
}
