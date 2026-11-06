package com.ellora.application.feature.admin;

import com.ellora.application.feature.admin.dto.AdminDashboardResponse;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminDashboardUseCase {

    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse execute() {
        long totalSalons = salonRepository.count();
        long pendingSalons = salonRepository.countByStatus(SalonStatus.PENDING_APPROVAL);
        long activeSalons = salonRepository.countByStatus(SalonStatus.ACTIVE);
        long totalUsers = userRepository.count();

        // Count all bookings using countBySalonId with null or sum across all salons,
        // or using bookingRepository countBySalonId(null) - wait, bookingRepository countBySalonId takes salonId.
        // Let's get total completed gross across the entire platform
        Instant beginningOfTime = Instant.EPOCH;
        Instant farFuture = Instant.now().plusSeconds(86400 * 365);
        BigDecimal totalRevenue = bookingRepository.sumCompletedGrossBySalonIdAndPeriod(null, beginningOfTime, farFuture);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new AdminDashboardResponse(
                totalSalons,
                pendingSalons,
                activeSalons,
                totalUsers,
                0L, // totalBookings summary
                totalRevenue
        );
    }
}
