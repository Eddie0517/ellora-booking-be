package com.ellora.application.feature.salon;

import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalonDetailsUseCase {

    private final SalonRepository salonRepository;

    @Transactional(readOnly = true)
    public SalonDetailsResponse execute(Long id) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon với mã định danh đã cung cấp"));

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
                salon.getStatus().name(),
                salon.getLatitude(),
                salon.getLongitude(),
                salon.getAverageRating(),
                salon.getTotalReviews(),
                salon.getSubscriptionTier()
        );
    }
}
