package com.ellora.application.feature.owner;

import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetOwnerSalonUseCase {

    private final SalonRepository salonRepository;
    private final UserRepository userRepository;

    @Autowired
    public GetOwnerSalonUseCase(SalonRepository salonRepository, UserRepository userRepository) {
        this.salonRepository = salonRepository;
        this.userRepository = userRepository;
    }

    public GetOwnerSalonUseCase(SalonRepository salonRepository) {
        this(salonRepository, null);
    }

    @Transactional(readOnly = true)
    public SalonDetailsResponse execute(UUID ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }

        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }

        Salon salon = salons.stream()
                .filter(s -> s.getStatus() == SalonStatus.ACTIVE || s.getStatus() == SalonStatus.PENDING_APPROVAL)
                .findFirst()
                .orElse(salons.get(0));

        boolean isOwnerLocked = userRepository != null && userRepository.findById(ownerId).map(User::isLocked).orElse(false);
        String finalStatus = isOwnerLocked ? SalonStatus.SUSPENDED.name() : (salon.getStatus() != null ? salon.getStatus().name() : null);

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
                finalStatus,
                salon.getLatitude(),
                salon.getLongitude(),
                salon.getAverageRating(),
                salon.getTotalReviews(),
                salon.getSubscriptionTier(),
                salon.getImageUrls(),
                salon.getContractUrl(),
                salon.getContractFileName(),
                salon.getContractFileType(),
                salon.getContractUploadedAt(),
                salon.isOverdue()
        );
    }
}
