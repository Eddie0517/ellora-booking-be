package com.ellora.api.controller;

import com.ellora.application.feature.category.GetServiceCategoriesUseCase;
import com.ellora.application.feature.category.ManageServiceCategoryUseCase;
import com.ellora.application.feature.category.dto.CreateCategoryRequest;
import com.ellora.application.feature.category.dto.ServiceCategoryResponse;
import com.ellora.application.feature.category.dto.UpdateCategoryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Service Categories", description = "Endpoints for browsing and managing nail service categories")
public class CategoryController {

    private final GetServiceCategoriesUseCase getServiceCategoriesUseCase;
    private final ManageServiceCategoryUseCase manageServiceCategoryUseCase;

    @GetMapping("/api/v1/service-categories")
    @Operation(summary = "List service categories", description = "Public endpoint returning active nail service categories")
    public ResponseEntity<List<ServiceCategoryResponse>> getCategories(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(getServiceCategoriesUseCase.execute(activeOnly));
    }

    @PostMapping("/api/v1/admin/service-categories")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create service category", description = "Admin endpoint to create a new nail service category")
    public ResponseEntity<ServiceCategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        ServiceCategoryResponse response = manageServiceCategoryUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/v1/admin/service-categories/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update service category", description = "Admin endpoint to update category details or active status")
    public ResponseEntity<ServiceCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(manageServiceCategoryUseCase.update(id, request));
    }

    @DeleteMapping("/api/v1/admin/service-categories/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate service category", description = "Admin endpoint to deactivate a service category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        manageServiceCategoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
