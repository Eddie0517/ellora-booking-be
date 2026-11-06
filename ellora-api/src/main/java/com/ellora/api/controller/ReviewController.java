package com.ellora.api.controller;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.review.CreateReviewUseCase;
import com.ellora.application.feature.review.GetSalonReviewsUseCase;
import com.ellora.application.feature.review.ReplyToReviewUseCase;
import com.ellora.application.feature.review.dto.CreateReviewRequest;
import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.application.feature.review.dto.ReviewReplyRequest;
import com.ellora.domain.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Endpoints for creating reviews, viewing salon reviews, and salon owner replies")
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final GetSalonReviewsUseCase getSalonReviewsUseCase;
    private final ReplyToReviewUseCase replyToReviewUseCase;

    @PostMapping("/api/v1/reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create review for a completed booking",
            description = "Allows an authenticated customer to submit a review with rating 1-5 for their completed appointment"
    )
    public ResponseEntity<ReviewDetailsResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal Object principal) {
        if (!(principal instanceof UUID customerId)) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập để đánh giá");
        }
        ReviewDetailsResponse response = createReviewUseCase.execute(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/salons/{salonId}/reviews")
    @Operation(
            summary = "Get reviews for a salon",
            description = "Public endpoint returning a paginated list of reviews for a specific salon"
    )
    public ResponseEntity<PagedResult<ReviewDetailsResponse>> getSalonReviews(
            @PathVariable Long salonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PagedResult<ReviewDetailsResponse> response = getSalonReviewsUseCase.execute(salonId, page, pageSize);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/reviews/{id}/reply")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Salon owner replies to review",
            description = "Allows a salon owner to reply to a review left on their salon"
    )
    public ResponseEntity<ReviewDetailsResponse> replyToReview(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody ReviewReplyRequest request,
            @AuthenticationPrincipal Object principal) {
        if (!(principal instanceof UUID ownerId)) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập để phản hồi");
        }
        ReviewDetailsResponse response = replyToReviewUseCase.execute(ownerId, reviewId, request);
        return ResponseEntity.ok(response);
    }
}
