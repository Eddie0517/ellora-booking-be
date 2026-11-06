package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.OwnerDashboardSummaryResponse;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.ReviewRepository;
import com.ellora.domain.repository.SalonRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOwnerDashboardUseCase {

    private final SalonRepository salonRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public OwnerDashboardSummaryResponse execute(UUID ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }

        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }

        Long salonId = salons.get(0).getId();
        long totalBookings = bookingRepository.countBySalonId(salonId);
        long pendingBookings = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.PENDING);
        long confirmedBookings = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.CONFIRMED);
        long completedBookings = bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.COMPLETED);

        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(vnZone);
        Instant startOfMonth = today.withDayOfMonth(1).atStartOfDay(vnZone).toInstant();
        Instant endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(vnZone).toInstant();

        BigDecimal monthlyRevenue = bookingRepository.sumCompletedGrossBySalonIdAndPeriod(salonId, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) {
            monthlyRevenue = BigDecimal.ZERO;
        }

        long monthlyCustomers = bookingRepository.countDistinctCompletedCustomersBySalonIdAndPeriod(salonId, startOfMonth, endOfMonth);

        Double avgRating = reviewRepository.calculateAverageRatingBySalonId(salonId);
        double averageRating = avgRating != null ? avgRating : 0.0;

        return new OwnerDashboardSummaryResponse(
                totalBookings,
                pendingBookings,
                confirmedBookings,
                completedBookings,
                monthlyRevenue,
                monthlyCustomers,
                averageRating
        );
    }
}
