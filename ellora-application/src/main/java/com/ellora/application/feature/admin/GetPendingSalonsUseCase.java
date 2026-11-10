package com.ellora.application.feature.admin;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPendingSalonsUseCase {

    private final SalonRepository salonRepository;

    @Transactional(readOnly = true)
    public PagedResult<SalonDetailsResponse> execute(int page, int pageSize) {
        return execute("PENDING_APPROVAL", page, pageSize);
    }

    @Transactional(readOnly = true)
    public PagedResult<SalonDetailsResponse> execute(String status, int page, int pageSize) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = (pageSize <= 0 || pageSize > 100) ? 20 : pageSize;
        int offset = normalizedPage * normalizedSize;

        List<Salon> salons;
        long totalItems;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) {
            salons = salonRepository.findAll(offset, normalizedSize);
            totalItems = salonRepository.count();
        } else {
            SalonStatus salonStatus;
            try {
                salonStatus = SalonStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                salonStatus = SalonStatus.PENDING_APPROVAL;
            }
            salons = salonRepository.findByStatus(salonStatus, offset, normalizedSize);
            totalItems = salonRepository.countByStatus(salonStatus);
        }

        List<SalonDetailsResponse> items = salons.stream().map(s -> new SalonDetailsResponse(
                s.getId(),
                s.getOwnerId(),
                s.getName(),
                s.getDescription(),
                s.getAddress(),
                s.getCity(),
                s.getDistrict(),
                s.getPhone(),
                s.getEmail(),
                s.getLogoUrl(),
                s.getStatus() != null ? s.getStatus().name() : null,
                s.getLatitude(),
                s.getLongitude(),
                s.getAverageRating(),
                s.getTotalReviews(),
                s.getSubscriptionTier(),
                s.getImageUrls(),
                s.getContractUrl(),
                s.getContractFileName(),
                s.getContractFileType(),
                s.getContractUploadedAt(),
                s.isOverdue()
        )).toList();

        return PagedResult.of(items, normalizedPage, normalizedSize, totalItems);
    }
}
