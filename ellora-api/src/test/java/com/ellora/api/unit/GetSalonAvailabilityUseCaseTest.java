package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.salon.GetSalonAvailabilityUseCase;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonClosureRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.WorkingHoursRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSalonAvailabilityUseCaseTest {
    @Mock SalonRepository salons;
    @Mock NailServiceRepository services;
    @Mock EmployeeRepository employees;
    @Mock WorkingHoursRepository hours;
    @Mock SalonClosureRepository closures;
    @Mock BookingRepository bookings;
    @InjectMocks GetSalonAvailabilityUseCase useCase;

    @Test
    void returnsNoTimesWhenTheSalonHasNoConfiguredWeeklyHours() {
        Salon salon = new Salon();
        salon.setId(8L);
        NailService service = new NailService();
        service.setId(12L);
        service.setSalonId(8L);
        when(salons.findById(8L)).thenReturn(Optional.of(salon));
        when(services.findById(12L)).thenReturn(Optional.of(service));
        when(hours.findBySalonId(8L)).thenReturn(List.of());

        assertThat(useCase.execute(8L, LocalDate.now().plusDays(1), 12L, null)).isEmpty();
    }
}
