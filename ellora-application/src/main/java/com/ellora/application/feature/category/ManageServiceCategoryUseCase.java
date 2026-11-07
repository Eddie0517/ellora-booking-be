package com.ellora.application.feature.category;

import com.ellora.application.feature.category.dto.CreateCategoryRequest;
import com.ellora.application.feature.category.dto.ServiceCategoryResponse;
import com.ellora.application.feature.category.dto.UpdateCategoryRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.ServiceCategory;
import com.ellora.domain.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageServiceCategoryUseCase {

    private final ServiceCategoryRepository serviceCategoryRepository;

    @Transactional
    public ServiceCategoryResponse create(CreateCategoryRequest request) {
        ServiceCategory category = new ServiceCategory(
                null,
                request.name().trim(),
                request.description(),
                request.iconUrl(),
                true
        );
        ServiceCategory saved = serviceCategoryRepository.save(category);
        return new ServiceCategoryResponse(saved.id(), saved.name(), saved.description(), saved.iconUrl(), saved.active());
    }

    @Transactional
    public ServiceCategoryResponse update(Long id, UpdateCategoryRequest request) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("CATEGORY_NOT_FOUND", "Không tìm thấy danh mục dịch vụ"));

        String name = request.name() != null ? request.name().trim() : existing.name();
        String desc = request.description() != null ? request.description() : existing.description();
        String icon = request.iconUrl() != null ? request.iconUrl() : existing.iconUrl();
        boolean active = request.active() != null ? request.active() : existing.active();

        ServiceCategory updated = new ServiceCategory(existing.id(), name, desc, icon, active);
        ServiceCategory saved = serviceCategoryRepository.save(updated);
        return new ServiceCategoryResponse(saved.id(), saved.name(), saved.description(), saved.iconUrl(), saved.active());
    }

    @Transactional
    public void delete(Long id) {
        ServiceCategory existing = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("CATEGORY_NOT_FOUND", "Không tìm thấy danh mục dịch vụ"));
        // Soft delete / deactivate
        ServiceCategory deactivated = new ServiceCategory(existing.id(), existing.name(), existing.description(), existing.iconUrl(), false);
        serviceCategoryRepository.save(deactivated);
    }
}
