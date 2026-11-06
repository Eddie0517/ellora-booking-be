package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.owner.SalonClosureUseCase;
import com.ellora.application.feature.owner.dto.CreateSalonClosureRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.SalonClosure;
import com.ellora.domain.repository.SalonClosureRepository;
import com.ellora.domain.repository.SalonRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalonClosureUseCaseTest {
    @Mock SalonRepository salons;
    @Mock SalonClosureRepository closures;

    @Test
    void rejectsDuplicateClosureDate() {
        UUID owner = UUID.randomUUID();
        Salon salon = new Salon();
        salon.setId(3L);
        LocalDate date = LocalDate.of(2027, 1, 1);
        when(salons.findByOwnerId(owner)).thenReturn(List.of(salon));
        when(closures.findBySalonIdAndDate(3L, date)).thenReturn(Optional.of(new SalonClosure(1L, 3L, date, null)));

        assertThatThrownBy(() -> new SalonClosureUseCase(salons, closures).create(owner,
                new CreateSalonClosureRequest(date, "")))
                .isInstanceOf(BusinessRuleException.class);
    }
}
