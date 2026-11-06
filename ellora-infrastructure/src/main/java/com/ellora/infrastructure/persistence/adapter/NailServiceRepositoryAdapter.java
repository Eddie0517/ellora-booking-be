package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.NailService;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.infrastructure.persistence.entity.NailServiceEntity;
import com.ellora.infrastructure.persistence.mapper.SalonMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataNailServiceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NailServiceRepositoryAdapter implements NailServiceRepository {

    private final SpringDataNailServiceRepository springDataNailServiceRepository;
    private final SalonMapper salonMapper;

    @Override
    public Optional<NailService> findById(Long id) {
        return springDataNailServiceRepository.findById(id).map(salonMapper::toDomain);
    }

    @Override
    public List<NailService> findBySalonIdAndActiveTrue(Long salonId) {
        return springDataNailServiceRepository.findBySalonIdAndActiveTrue(salonId)
                .stream()
                .map(salonMapper::toDomain)
                .toList();
    }

    @Override
    public NailService save(NailService service) {
        NailServiceEntity entity = salonMapper.toEntity(service);
        NailServiceEntity saved = springDataNailServiceRepository.save(entity);
        return salonMapper.toDomain(saved);
    }
}
