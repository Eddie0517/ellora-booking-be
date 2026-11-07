package com.ellora.application.feature.category;

import com.ellora.application.feature.category.dto.ServiceCategoryResponse;
import com.ellora.domain.model.ServiceCategory;
import com.ellora.domain.repository.ServiceCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetServiceCategoriesUseCase {

    private final ServiceCategoryRepository serviceCategoryRepository;

    public List<ServiceCategoryResponse> execute(boolean activeOnly) {
        List<ServiceCategory> categories = activeOnly
                ? serviceCategoryRepository.findAllActive()
                : serviceCategoryRepository.findAll();

        return categories.stream()
                .map(c -> new ServiceCategoryResponse(c.id(), c.name(), c.description(), c.iconUrl(), c.active()))
                .toList();
    }
}
