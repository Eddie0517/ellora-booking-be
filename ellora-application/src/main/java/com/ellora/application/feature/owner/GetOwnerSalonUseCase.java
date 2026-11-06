package com.ellora.application.feature.owner;

import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOwnerSalonUseCase {

    private final SalonRepository salonRepository;

    @Transactional(readOnly = true)
    public SalonDetailsResponse execute(UUID ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }

        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }

        Salon salon = salons.get(0);
        return new SalonDetailsResponse(
                salon.getId(),
                salon.getOwnerId(),
                salon.getName(),
                salon.getDescription(),
                salon.getAddress(),
                salon.getCity(),
                salon.getDistrict(),
                salon.getPhone(),
                salon.getEmail(),
                salon.getLogoUrl(),
                salon.getStatus() != null ? salon.getStatus().name() : null,
                salon.getLatitude(),
                salon.getLongitude(),
                salon.getAverageRating(),
                salon.getTotalReviews()
        );
    }
}
