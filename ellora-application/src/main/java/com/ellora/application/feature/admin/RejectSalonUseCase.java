package com.ellora.application.feature.admin;

import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectSalonUseCase {

    private final SalonRepository salonRepository;

    @Transactional
    public void execute(Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        salon.suspend();
        salonRepository.save(salon);
    }
}
