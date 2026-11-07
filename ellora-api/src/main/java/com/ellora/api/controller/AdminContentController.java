package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.ManageAdminContentUseCase;
import com.ellora.application.feature.admin.dto.AdminEditReviewRequest;
import com.ellora.application.feature.admin.dto.AdminReplyReviewRequest;
import com.ellora.application.feature.admin.dto.ContentDetailResponse;
import com.ellora.application.feature.admin.dto.ContentSalonResponse;
import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/content")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Content", description = "Endpoints for platform admin to moderate salon media and user reviews")
public class AdminContentController {

    private final ManageAdminContentUseCase manageAdminContentUseCase;

    @GetMapping("/salons")
    @Operation(summary = "List salons for content moderation")
    public ResponseEntity<PagedResult<ContentSalonResponse>> listSalons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageAdminContentUseCase.listSalons(page, size));
    }

    @GetMapping("/salons/{id}")
    @Operation(summary = "Get salon media details for content moderation")
    public ResponseEntity<ContentDetailResponse> getSalonDetail(@PathVariable Long id) {
        return ResponseEntity.ok(manageAdminContentUseCase.getSalonDetail(id));
    }

    @GetMapping("/salons/{id}/reviews")
    @Operation(summary = "Get salon reviews for moderation")
    public ResponseEntity<PagedResult<ReviewDetailsResponse>> getSalonReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageAdminContentUseCase.getSalonReviews(id, page, size));
    }

    @PostMapping(value = "/salons/{salonId}/photos/{kind}/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace photo for a salon")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable Long salonId,
            @PathVariable String kind,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "oldUrl", required = false) String oldUrl) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("INVALID_FILE", "Vui lòng chọn tệp tin hình ảnh");
        }
        try {
            String url = manageAdminContentUseCase.uploadPhoto(
                    salonId, kind, id, file.getOriginalFilename(),
                    file.getInputStream(), file.getSize(), file.getContentType(), oldUrl
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(url);
        } catch (IOException e) {
            throw new BusinessRuleException("FILE_READ_ERROR", "Không thể đọc dữ liệu hình ảnh");
        }
    }

    @DeleteMapping("/salons/{salonId}/photos/{kind}/{id}")
    @Operation(summary = "Remove photo from salon")
    public ResponseEntity<Void> removePhoto(
            @PathVariable Long salonId,
            @PathVariable String kind,
            @PathVariable Long id,
            @RequestParam(value = "url", required = false) String url) {
        manageAdminContentUseCase.removePhoto(salonId, kind, id, url);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reviews/{id}")
    @Operation(summary = "Admin edits customer review")
    public ResponseEntity<Void> editReview(
            @PathVariable Long id,
            @Valid @RequestBody AdminEditReviewRequest request) {
        manageAdminContentUseCase.editReview(id, request.rating(), request.comment());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Admin deletes review")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        manageAdminContentUseCase.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reviews/{id}/reply")
    @Operation(summary = "Admin edits salon reply")
    public ResponseEntity<Void> replyReview(
            @PathVariable Long id,
            @Valid @RequestBody AdminReplyReviewRequest request) {
        manageAdminContentUseCase.replyReview(id, request.reply());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{id}/reply")
    @Operation(summary = "Admin deletes salon reply")
    public ResponseEntity<Void> deleteReply(@PathVariable Long id) {
        manageAdminContentUseCase.deleteReply(id);
        return ResponseEntity.noContent().build();
    }
}
