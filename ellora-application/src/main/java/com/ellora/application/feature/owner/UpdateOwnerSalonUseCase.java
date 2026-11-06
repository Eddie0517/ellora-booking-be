package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.UpdateOwnerSalonRequest;
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
public class UpdateOwnerSalonUseCase {

    private final SalonRepository salonRepository;

    @Transactional
    public SalonDetailsResponse execute(UUID ownerId, UpdateOwnerSalonRequest request) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }

        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }

        Salon salon = salons.get(0);
        salon.updateProfile(
                request.name(),
                request.description(),
                request.address(),
                request.city(),
                request.district(),
                request.phone(),
                request.email(),
                request.logoUrl()
        );

        Salon savedSalon = salonRepository.save(salon);
        return new SalonDetailsResponse(
                savedSalon.getId(),
                savedSalon.getOwnerId(),
                savedSalon.getName(),
                savedSalon.getDescription(),
                savedSalon.getAddress(),
                savedSalon.getCity(),
                savedSalon.getDistrict(),
                savedSalon.getPhone(),
                savedSalon.getEmail(),
                savedSalon.getLogoUrl(),
                savedSalon.getStatus() != null ? savedSalon.getStatus().name() : null,
                savedSalon.getLatitude(),
                savedSalon.getLongitude(),
                savedSalon.getAverageRating(),
                savedSalon.getTotalReviews()
        );
    }
}
