package com.ellora.api.controller;

import com.ellora.application.feature.owner.ManageOwnerPhotosUseCase;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/owner/photos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Owner Photos", description = "Endpoints for salon owners to manage salon cover, gallery, services, and employee photos")
public class OwnerPhotoController {

    private final ManageOwnerPhotosUseCase manageOwnerPhotosUseCase;

    private UUID getOwnerId(Object principal) {
        if (!(principal instanceof UUID ownerId)) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập với tài khoản chủ salon");
        }
        return ownerId;
    }

    @PostMapping(value = "/{kind}/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload photo for cover, service, or employee")
    public ResponseEntity<String> uploadPhoto(
            @AuthenticationPrincipal Object principal,
            @PathVariable String kind,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("INVALID_FILE", "Vui lòng chọn tệp tin hình ảnh");
        }
        try {
            String url = manageOwnerPhotosUseCase.uploadPhoto(
                    getOwnerId(principal),
                    kind,
                    id,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(url);
        } catch (IOException e) {
            throw new BusinessRuleException("FILE_READ_ERROR", "Không thể đọc dữ liệu hình ảnh");
        }
    }

    @DeleteMapping("/{kind}/{id}")
    @Operation(summary = "Delete photo for cover, service, or employee")
    public ResponseEntity<Void> removePhoto(
            @AuthenticationPrincipal Object principal,
            @PathVariable String kind,
            @PathVariable Long id) {
        manageOwnerPhotosUseCase.removePhoto(getOwnerId(principal), kind, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/gallery/{id}/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple photos to salon gallery")
    public ResponseEntity<List<String>> uploadGallery(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessRuleException("INVALID_FILE", "Vui lòng chọn ít nhất một hình ảnh");
        }
        try {
            List<ManageOwnerPhotosUseCase.UploadFileInput> inputs = new ArrayList<>();
            for (MultipartFile file : files) {
                inputs.add(new ManageOwnerPhotosUseCase.UploadFileInput(
                        file.getOriginalFilename(),
                        file.getInputStream(),
                        file.getSize(),
                        file.getContentType()
                ));
            }
            List<String> urls = manageOwnerPhotosUseCase.uploadGallery(getOwnerId(principal), id, inputs);
            return ResponseEntity.status(HttpStatus.CREATED).body(urls);
        } catch (IOException e) {
            throw new BusinessRuleException("FILE_READ_ERROR", "Không thể đọc dữ liệu hình ảnh");
        }
    }

    @DeleteMapping("/gallery")
    @Operation(summary = "Remove photo from salon gallery")
    public ResponseEntity<Void> removeGallery(
            @AuthenticationPrincipal Object principal,
            @RequestParam("url") String url) {
        manageOwnerPhotosUseCase.removeGallery(getOwnerId(principal), url);
        return ResponseEntity.noContent().build();
    }
}
