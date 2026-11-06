package com.ellora.application.feature.review;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Review;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.ReviewRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalonReviewsUseCase {

    private final ReviewRepository reviewRepository;
    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final NailServiceRepository nailServiceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public PagedResult<ReviewDetailsResponse> execute(Long salonId, int page, int pageSize) {
        if (salonId == null) {
            throw new BusinessRuleException("INVALID_SALON", "Mã salon không được để trống");
        }

        if (!salonRepository.findById(salonId).isPresent()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon");
        }

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = (pageSize <= 0 || pageSize > 100) ? 10 : pageSize;

        List<Review> reviews = reviewRepository.findBySalonId(salonId, normalizedPage, normalizedSize);
        long totalItems = reviewRepository.countBySalonId(salonId);

        List<ReviewDetailsResponse> responseList = reviews.stream().map(review -> {
            User customer = userRepository.findById(review.getCustomerId()).orElse(null);
            Booking booking = bookingRepository.findById(review.getBookingId()).orElse(null);

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
                    review.getId(),
                    review.getCustomerId(),
                    customer != null ? customer.getFullName() : null,
                    customer != null ? customer.getAvatarUrl() : null,
                    review.getSalonId(),
                    review.getBookingId(),
                    serviceName,
                    employeeName,
                    review.getRating(),
                    review.getComment(),
                    review.getSalonReply(),
                    review.getSalonRepliedAt(),
                    review.getCreatedAt()
            );
        }).toList();

        return PagedResult.of(responseList, normalizedPage, normalizedSize, totalItems);
    }
}
