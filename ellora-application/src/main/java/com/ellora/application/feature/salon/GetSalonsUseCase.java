package com.ellora.application.feature.salon;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.salon.dto.SalonSummaryResponse;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalonsUseCase {

    private final SalonRepository salonRepository;

    @Transactional(readOnly = true)
    public PagedResult<SalonSummaryResponse> execute(String city, String keyword, int page, int pageSize) {
        return execute(city, keyword, null, null, null, "newest", page, pageSize);
    }

    @Transactional(readOnly = true)
    public PagedResult<SalonSummaryResponse> execute(
            String city, String keyword,
            Double latitude, Double longitude, Double radiusKm, String sort,
            int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        int offset = (safePage - 1) * safePageSize;

        Double validLat = (latitude != null && latitude >= -90.0 && latitude <= 90.0) ? latitude : null;
        Double validLng = (longitude != null && longitude >= -180.0 && longitude <= 180.0) ? longitude : null;
        if (validLat == null || validLng == null) {
            validLat = null;
            validLng = null;
        }

        List<Salon> salons;
        long totalItems;
        if (validLat == null && validLng == null && radiusKm == null && (sort == null || "newest".equalsIgnoreCase(sort))) {
            salons = salonRepository.searchActiveSalons(city, keyword, offset, safePageSize);
            totalItems = salonRepository.countActiveSalons(city, keyword);
        } else {
            salons = salonRepository.searchActiveSalons(city, keyword, validLat, validLng, radiusKm, sort, offset, safePageSize);
            totalItems = salonRepository.countActiveSalons(city, keyword, validLat, validLng, radiusKm);
        }

        List<SalonSummaryResponse> items = salons.stream()
                .map(s -> new SalonSummaryResponse(
                        s.getId(),
                        s.getName(),
                        s.getAddress(),
                        s.getCity(),
                        s.getDistrict(),
                        s.getPhone(),
                        s.getLogoUrl(),
                        s.getAverageRating(),
                        s.getTotalReviews(),
                        s.getSubscriptionTier(),
                        s.getLatitude(),
                        s.getLongitude(),
                        s.getDistanceKm()
                ))
                .toList();

        return PagedResult.of(items, safePage, safePageSize, totalItems);
    }
}
