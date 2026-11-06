package com.ellora.application.feature.admin;

import com.ellora.domain.constant.Role;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveSalonUseCase {

    private final SalonRepository salonRepository;
    private final UserRepository userRepository;

    @Transactional
    public void execute(Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        salon.approve();
        salonRepository.save(salon);

        if (salon.getOwnerId() != null) {
            userRepository.findById(salon.getOwnerId()).ifPresent(owner -> {
                if (owner.getRole() == Role.CUSTOMER) {
                    owner.setRole(Role.SALON_OWNER);
                    userRepository.save(owner);
                }
            });
        }
    }
}
