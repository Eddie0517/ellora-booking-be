package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.CreateServiceRequest;
import com.ellora.application.feature.owner.dto.ServiceResponse;
import com.ellora.application.feature.owner.dto.UpdateServiceRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageOwnerServicesUseCase {

    private final SalonRepository salonRepository;
    private final NailServiceRepository nailServiceRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServices(UUID ownerId) {
        Salon salon = getOwnerSalon(ownerId);
        return nailServiceRepository.findBySalonId(salon.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ServiceResponse createService(UUID ownerId, CreateServiceRequest request) {
        Salon salon = getOwnerSalon(ownerId);

        NailService service = NailService.createNew(
                salon.getId(),
                request.categoryId(),
                request.name(),
                request.description(),
                request.price(),
                request.durationMinutes(),
                request.imageUrl()
        );

        NailService saved = nailServiceRepository.save(service);
        return mapToResponse(saved);
    }

    @Transactional
    public ServiceResponse updateService(UUID ownerId, Long serviceId, UpdateServiceRequest request) {
        Salon salon = getOwnerSalon(ownerId);

        NailService service = nailServiceRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));

        if (!service.getSalonId().equals(salon.getId())) {
            throw new BusinessRuleException("FORBIDDEN", "Dịch vụ không thuộc salon của bạn");
        }

        service.setName(request.name().trim());
        service.setDescription(request.description() != null ? request.description().trim() : null);
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setCategoryId(request.categoryId());
        if (request.imageUrl() != null) {
            service.setImageUrl(request.imageUrl().trim());
        }
        if (request.active() != null) {
            service.setActive(request.active());
        }

        NailService updated = nailServiceRepository.save(service);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteService(UUID ownerId, Long serviceId) {
        Salon salon = getOwnerSalon(ownerId);

        NailService service = nailServiceRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));

        if (!service.getSalonId().equals(salon.getId())) {
            throw new BusinessRuleException("FORBIDDEN", "Dịch vụ không thuộc salon của bạn");
        }

        // Soft delete
        service.setActive(false);
        nailServiceRepository.save(service);
    }

    private Salon getOwnerSalon(UUID ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }
        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }
        return salons.get(0);
    }

    private ServiceResponse mapToResponse(NailService s) {
        return new ServiceResponse(
                s.getId(),
                s.getSalonId(),
                s.getCategoryId(),
                s.getName(),
                s.getDescription(),
                s.getPrice(),
                s.getDurationMinutes(),
                s.getImageUrl(),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
