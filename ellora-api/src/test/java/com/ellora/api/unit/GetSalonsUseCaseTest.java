package com.ellora.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.salon.GetSalonsUseCase;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetSalonsUseCaseTest {
    @Test
    void exposesSubscriptionTierInSearchResults() {
        SalonRepository repository = mock(SalonRepository.class);
        Salon salon = new Salon();
        salon.setId(12L);
        salon.setSubscriptionTier("VIP");
        when(repository.searchActiveSalons(null, null, 0, 10)).thenReturn(List.of(salon));
        when(repository.countActiveSalons(null, null)).thenReturn(1L);

        var result = new GetSalonsUseCase(repository).execute(null, null, 1, 10);

        assertEquals("VIP", result.items().get(0).subscriptionTier());
    }
}
