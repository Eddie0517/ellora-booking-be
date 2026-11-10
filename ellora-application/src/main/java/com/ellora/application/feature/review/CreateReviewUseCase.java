package com.ellora.application.feature.review;

import com.ellora.application.feature.review.dto.CreateReviewRequest;
import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.domain.constant.BookingStatus;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final NailServiceRepository nailServiceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ReviewDetailsResponse execute(UUID customerId, CreateReviewRequest request) {
        if (customerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập để đánh giá");
        }

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new BusinessRuleException("BOOKING_NOT_FOUND", "Không tìm thấy lịch hẹn"));

        if (booking.getCustomerId() == null || !booking.getCustomerId().equals(customerId)) {
            throw new BusinessRuleException("FORBIDDEN", "Bạn chỉ có thể đánh giá lịch hẹn của chính mình");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessRuleException("BOOKING_NOT_COMPLETED", "Chỉ có thể đánh giá lịch hẹn đã hoàn thành dịch vụ");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BusinessRuleException("ALREADY_REVIEWED", "Lịch hẹn này đã được đánh giá trước đó");
        }

        Salon salon = salonRepository.findById(booking.getSalonId())
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        Review review = Review.create(
                customerId,
                booking.getSalonId(),
                booking.getId(),
                request.rating(),
                request.comment()
        );
        Review savedReview = reviewRepository.save(review);

        // Update salon average rating and review count
        Double avgRating = reviewRepository.calculateAverageRatingBySalonId(salon.getId());
        long totalReviews = reviewRepository.countBySalonId(salon.getId());
        BigDecimal scaledAvg = avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        salon.updateRatingStats(scaledAvg, (int) totalReviews);
        salonRepository.save(salon);

        // Khi người dùng đánh giá không hài lòng (<= 2 sao), chuyển thành phản ánh lịch hẹn sang luồng admin xử lý
        if (request.rating() <= 2) {
            String note = (request.comment() != null && !request.comment().isBlank())
                    ? request.comment().trim()
                    : "Khách hàng đánh giá không hài lòng (" + request.rating() + "/5 sao)";
            booking.dispute(note);
            booking.setCompletionRespondedAt(Instant.now());
            bookingRepository.save(booking);
        }

        User customer = userRepository.findById(customerId).orElse(null);
        NailService service = booking.getServiceId() != null ? nailServiceRepository.findById(booking.getServiceId()).orElse(null) : null;
        Employee employee = booking.getEmployeeId() != null ? employeeRepository.findById(booking.getEmployeeId()).orElse(null) : null;

        return new ReviewDetailsResponse(
                savedReview.getId(),
                customerId,
                customer != null ? customer.getFullName() : null,
                customer != null ? customer.getAvatarUrl() : null,
                salon.getId(),
                booking.getId(),
                service != null ? service.getName() : null,
                employee != null ? employee.getFullName() : null,
                savedReview.getRating(),
                savedReview.getComment(),
                savedReview.getSalonReply(),
                savedReview.getSalonRepliedAt(),
                savedReview.getCreatedAt()
        );
    }
}
