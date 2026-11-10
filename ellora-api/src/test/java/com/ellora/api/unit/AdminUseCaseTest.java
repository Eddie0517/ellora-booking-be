package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.ApproveSalonUseCase;
import com.ellora.application.feature.admin.GetAdminDashboardUseCase;
import com.ellora.application.feature.admin.GetAdminUsersUseCase;
import com.ellora.application.feature.admin.GetPendingSalonsUseCase;
import com.ellora.application.feature.admin.LockUserUseCase;
import com.ellora.application.feature.admin.RejectSalonUseCase;
import com.ellora.application.feature.admin.dto.AdminDashboardResponse;
import com.ellora.application.feature.admin.dto.AdminUserResponse;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.constant.Role;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUseCaseTest {

    @Mock
    private SalonRepository salonRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private com.ellora.application.common.port.ImageStorage imageStorage;

    private GetAdminDashboardUseCase getAdminDashboardUseCase;
    private GetPendingSalonsUseCase getPendingSalonsUseCase;
    private ApproveSalonUseCase approveSalonUseCase;
    private RejectSalonUseCase rejectSalonUseCase;
    private GetAdminUsersUseCase getAdminUsersUseCase;
    private LockUserUseCase lockUserUseCase;

    @BeforeEach
    void setUp() {
        getAdminDashboardUseCase = new GetAdminDashboardUseCase(salonRepository, userRepository, bookingRepository);
        getPendingSalonsUseCase = new GetPendingSalonsUseCase(salonRepository);
        approveSalonUseCase = new ApproveSalonUseCase(salonRepository, userRepository, imageStorage);
        rejectSalonUseCase = new RejectSalonUseCase(salonRepository);
        getAdminUsersUseCase = new GetAdminUsersUseCase(userRepository);
        lockUserUseCase = new LockUserUseCase(userRepository, refreshTokenRepository);
    }

    @Test
    @DisplayName("AdminDashboard: Returns aggregated counts and revenue")
    void getAdminDashboard_success() {
        when(salonRepository.count()).thenReturn(10L);
        when(salonRepository.countByStatus(SalonStatus.PENDING_APPROVAL)).thenReturn(2L);
        when(salonRepository.countByStatus(SalonStatus.ACTIVE)).thenReturn(8L);
        when(userRepository.count()).thenReturn(100L);
        when(bookingRepository.sumCompletedGrossBySalonIdAndPeriod(any(), any(), any())).thenReturn(BigDecimal.valueOf(50000000));

        AdminDashboardResponse response = getAdminDashboardUseCase.execute();

        assertThat(response.totalSalons()).isEqualTo(10L);
        assertThat(response.pendingSalons()).isEqualTo(2L);
        assertThat(response.activeSalons()).isEqualTo(8L);
        assertThat(response.totalUsers()).isEqualTo(100L);
        assertThat(response.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(50000000));
    }

    @Test
    @DisplayName("ApproveSalon: Approves salon and promotes owner to SALON_OWNER")
    void approveSalon_success() {
        UUID ownerId = UUID.randomUUID();
        Salon salon = new Salon();
        salon.setId(1L);
        salon.setOwnerId(ownerId);
        salon.setStatus(SalonStatus.PENDING_APPROVAL);

        User owner = new User();
        owner.setId(ownerId);
        owner.setRole(Role.CUSTOMER);

        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        approveSalonUseCase.execute(1L);

        assertThat(salon.getStatus()).isEqualTo(SalonStatus.ACTIVE);
        assertThat(owner.getRole()).isEqualTo(Role.SALON_OWNER);
        verify(salonRepository).save(salon);
        verify(userRepository).save(owner);
    }

    @Test
    @DisplayName("RejectSalon: Suspends salon")
    void rejectSalon_success() {
        Salon salon = new Salon();
        salon.setId(2L);
        salon.setStatus(SalonStatus.PENDING_APPROVAL);

        when(salonRepository.findById(2L)).thenReturn(Optional.of(salon));

        rejectSalonUseCase.execute(2L);

        assertThat(salon.getStatus()).isEqualTo(SalonStatus.SUSPENDED);
        verify(salonRepository).save(salon);
    }

    @Test
    @DisplayName("LockUser: Locks user and deletes active refresh tokens")
    void lockUser_locksAccount() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setLocked(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserResponse response = lockUserUseCase.execute(userId, true);

        assertThat(response.locked()).isTrue();
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    @DisplayName("GetPendingSalons: Returns paginated pending salons")
    void getPendingSalons_success() {
        Salon salon = new Salon();
        salon.setId(5L);
        salon.setName("Pending Salon");
        salon.setStatus(SalonStatus.PENDING_APPROVAL);

        when(salonRepository.findByStatus(SalonStatus.PENDING_APPROVAL, 0, 20)).thenReturn(List.of(salon));
        when(salonRepository.countByStatus(SalonStatus.PENDING_APPROVAL)).thenReturn(1L);

        PagedResult<SalonDetailsResponse> result = getPendingSalonsUseCase.execute(0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalItems()).isEqualTo(1L);
        assertThat(result.items().get(0).name()).isEqualTo("Pending Salon");
    }
}
