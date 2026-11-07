package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.ServiceCategory;
import com.ellora.domain.repository.ServiceCategoryRepository;
import com.ellora.infrastructure.persistence.entity.ServiceCategoryEntity;
import com.ellora.infrastructure.persistence.repository.SpringDataServiceCategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceCategoryRepositoryAdapter implements ServiceCategoryRepository {

    private final SpringDataServiceCategoryRepository repository;

    private ServiceCategory toDomain(ServiceCategoryEntity entity) {
        return new ServiceCategory(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIconUrl(),
                entity.isActive()
        );
    }

    private ServiceCategoryEntity toEntity(ServiceCategory domain) {
        ServiceCategoryEntity entity = new ServiceCategoryEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setIconUrl(domain.iconUrl());
        entity.setActive(domain.active());
        return entity;
    }

    @Override
    public List<ServiceCategory> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<ServiceCategory> findAll() {
        return repository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ServiceCategory> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public ServiceCategory save(ServiceCategory category) {
        ServiceCategoryEntity entity = toEntity(category);
        return toDomain(repository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
