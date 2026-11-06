package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.owner.WorkingHoursUseCase;
import com.ellora.application.feature.owner.dto.WorkingHourDto;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.WorkingHoursRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingHoursUseCaseTest {
    @Mock SalonRepository salons;
    @Mock WorkingHoursRepository hours;

    @Test
    void rejectsIncompleteWeeklySchedule() {
        UUID owner = UUID.randomUUID();
        Salon salon = new Salon();
        salon.setId(5L);
        when(salons.findByOwnerId(owner)).thenReturn(List.of(salon));
        var input = List.of(new WorkingHourDto(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), false));
        assertThatThrownBy(() -> new WorkingHoursUseCase(salons, hours).save(owner, input))
                .isInstanceOf(BusinessRuleException.class);
    }
}
