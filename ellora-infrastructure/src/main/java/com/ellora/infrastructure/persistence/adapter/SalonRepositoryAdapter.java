package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.infrastructure.persistence.entity.SalonEntity;
import com.ellora.infrastructure.persistence.mapper.SalonMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataSalonRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalonRepositoryAdapter implements SalonRepository {

    private final SpringDataSalonRepository springDataSalonRepository;
    private final SalonMapper salonMapper;

    @Override
    public Optional<Salon> findById(Long id) {
        return springDataSalonRepository.findById(id).map(salonMapper::toDomain);
    }

    @Override
    public List<Salon> findByStatus(SalonStatus status, int offset, int limit) {
        int pageNumber = offset / Math.max(1, limit);
        Page<SalonEntity> page = springDataSalonRepository.findByStatus(status.name(), PageRequest.of(pageNumber, limit));
        return page.getContent().stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public List<Salon> searchActiveSalons(String city, String keyword, int offset, int limit) {
        int pageNumber = offset / Math.max(1, limit);
        Page<SalonEntity> page = springDataSalonRepository.searchActiveSalons(city, keyword, PageRequest.of(pageNumber, limit));
        return page.getContent().stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public long countActiveSalons(String city, String keyword) {
        Page<SalonEntity> page = springDataSalonRepository.searchActiveSalons(city, keyword, PageRequest.of(0, 1));
        return page.getTotalElements();
    }

    @Override
    public List<Salon> findByOwnerId(UUID ownerId) {
        return springDataSalonRepository.findByOwnerId(ownerId).stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public Salon save(Salon salon) {
        SalonEntity entity = salonMapper.toEntity(salon);
        SalonEntity saved = springDataSalonRepository.save(entity);
        return salonMapper.toDomain(saved);
    }
}
