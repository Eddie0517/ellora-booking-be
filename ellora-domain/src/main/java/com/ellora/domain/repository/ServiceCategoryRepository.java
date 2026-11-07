package com.ellora.domain.repository;

import com.ellora.domain.model.ServiceCategory;
import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository {
    List<ServiceCategory> findAllActive();
    List<ServiceCategory> findAll();
    Optional<ServiceCategory> findById(Long id);
    ServiceCategory save(ServiceCategory category);
    void deleteById(Long id);
}
