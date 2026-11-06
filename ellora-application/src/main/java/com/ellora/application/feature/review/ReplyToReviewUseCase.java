package com.ellora.application.feature.review;

import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.application.feature.review.dto.ReviewReplyRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Review;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.ReviewRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyToReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final NailServiceRepository nailServiceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ReviewDetailsResponse execute(UUID currentUserId, Long reviewId, ReviewReplyRequest request) {
        if (currentUserId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập để phản hồi đánh giá");
        }
        if (reviewId == null) {
            throw new BusinessRuleException("INVALID_REVIEW", "Mã đánh giá không được để trống");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessRuleException("REVIEW_NOT_FOUND", "Không tìm thấy đánh giá"));

        Salon salon = salonRepository.findById(review.getSalonId())
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        if (salon.getOwnerId() == null || !salon.getOwnerId().equals(currentUserId)) {
            throw new BusinessRuleException("FORBIDDEN", "Bạn không có quyền phản hồi đánh giá của salon này");
        }

        review.reply(request.reply());
        Review updatedReview = reviewRepository.save(review);

        User customer = userRepository.findById(updatedReview.getCustomerId()).orElse(null);
        Booking booking = bookingRepository.findById(updatedReview.getBookingId()).orElse(null);

        String serviceName = null;
        String employeeName = null;
        if (booking != null) {
            if (booking.getServiceId() != null) {
                serviceName = nailServiceRepository.findById(booking.getServiceId())
                        .map(NailService::getName)
                        .orElse(null);
            }
            if (booking.getEmployeeId() != null) {
                employeeName = employeeRepository.findById(booking.getEmployeeId())
                        .map(Employee::getFullName)
                        .orElse(null);
            }
        }

        return new ReviewDetailsResponse(
                updatedReview.getId(),
                updatedReview.getCustomerId(),
                customer != null ? customer.getFullName() : null,
                customer != null ? customer.getAvatarUrl() : null,
                updatedReview.getSalonId(),
                updatedReview.getBookingId(),
                serviceName,
                employeeName,
                updatedReview.getRating(),
                updatedReview.getComment(),
                updatedReview.getSalonReply(),
                updatedReview.getSalonRepliedAt(),
                updatedReview.getCreatedAt()
        );
    }
}
