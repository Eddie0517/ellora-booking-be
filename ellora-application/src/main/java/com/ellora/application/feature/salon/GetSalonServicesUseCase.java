package com.ellora.application.feature.salon;

import com.ellora.application.feature.salon.dto.NailServiceResponse;
import com.ellora.domain.model.NailService;
import com.ellora.domain.repository.NailServiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalonServicesUseCase {

    private final NailServiceRepository nailServiceRepository;

    @Transactional(readOnly = true)
    public List<NailServiceResponse> execute(Long salonId) {
        List<NailService> services = nailServiceRepository.findBySalonIdAndActiveTrue(salonId);
        return services.stream()
                .map(s -> new NailServiceResponse(
                        s.getId(),
                        s.getSalonId(),
                        s.getCategoryId(),
                        s.getName(),
                        s.getDescription(),
                        s.getPrice(),
                        s.getDurationMinutes(),
                        s.getImageUrl(),
                        s.isActive()
                ))
                .toList();
    }
}
