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
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        int offset = (safePage - 1) * safePageSize;

        List<Salon> salons = salonRepository.searchActiveSalons(city, keyword, offset, safePageSize);
        long totalItems = salonRepository.countActiveSalons(city, keyword);

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
                        s.getTotalReviews()
                ))
                .toList();

        return PagedResult.of(items, safePage, safePageSize, totalItems);
    }
}
