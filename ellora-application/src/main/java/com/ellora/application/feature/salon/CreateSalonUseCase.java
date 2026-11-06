package com.ellora.application.feature.salon;

import com.ellora.application.feature.salon.dto.CreateSalonRequest;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSalonUseCase {

    private final SalonRepository salonRepository;

    @Transactional
    public SalonDetailsResponse execute(UUID ownerId, CreateSalonRequest request) {
        Salon salon = Salon.createNew(
                ownerId,
                request.name(),
                request.address(),
                request.city(),
                request.district(),
                request.phone(),
                request.email(),
                request.description()
        );

        Salon saved = salonRepository.save(salon);

        return new SalonDetailsResponse(
                saved.getId(),
                saved.getOwnerId(),
                saved.getName(),
                saved.getDescription(),
                saved.getAddress(),
                saved.getCity(),
                saved.getDistrict(),
                saved.getPhone(),
                saved.getEmail(),
                saved.getLogoUrl(),
                saved.getStatus().name(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getAverageRating(),
                saved.getTotalReviews()
        );
    }
}
