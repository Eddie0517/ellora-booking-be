package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.booking.dto.BookingDetailsResponse;
import com.ellora.application.feature.booking.BookingResponseMapper;
import com.ellora.application.feature.owner.GetOwnerBookingsUseCase;
import com.ellora.application.feature.owner.GetOwnerDashboardUseCase;
import com.ellora.application.feature.owner.GetOwnerSalonUseCase;
import com.ellora.application.feature.owner.ManageOwnerEmployeesUseCase;
import com.ellora.application.feature.owner.ManageOwnerServicesUseCase;
import com.ellora.application.feature.owner.UpdateOwnerSalonUseCase;
import com.ellora.application.feature.owner.dto.CreateEmployeeRequest;
import com.ellora.application.feature.owner.dto.CreateServiceRequest;
import com.ellora.application.feature.owner.dto.EmployeeResponse;
import com.ellora.application.feature.owner.dto.OwnerDashboardSummaryResponse;
import com.ellora.application.feature.owner.dto.ServiceResponse;
import com.ellora.application.feature.owner.dto.UpdateEmployeeRequest;
import com.ellora.application.feature.owner.dto.UpdateOwnerSalonRequest;
import com.ellora.application.feature.owner.dto.UpdateServiceRequest;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.ReviewRepository;
import com.ellora.domain.repository.SalonRepository;
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
class OwnerUseCaseTest {

    @Mock
    private SalonRepository salonRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private NailServiceRepository nailServiceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private com.ellora.domain.repository.UserRepository userRepository;

    private GetOwnerSalonUseCase getOwnerSalonUseCase;
    private UpdateOwnerSalonUseCase updateOwnerSalonUseCase;
    private GetOwnerDashboardUseCase getOwnerDashboardUseCase;
    private ManageOwnerServicesUseCase manageOwnerServicesUseCase;
    private ManageOwnerEmployeesUseCase manageOwnerEmployeesUseCase;
    private GetOwnerBookingsUseCase getOwnerBookingsUseCase;

    private final UUID ownerId = UUID.randomUUID();
    private final Long salonId = 10L;
    private Salon sampleSalon;

    @BeforeEach
    void setUp() {
        getOwnerSalonUseCase = new GetOwnerSalonUseCase(salonRepository);
        updateOwnerSalonUseCase = new UpdateOwnerSalonUseCase(salonRepository);
        getOwnerDashboardUseCase = new GetOwnerDashboardUseCase(salonRepository, bookingRepository, reviewRepository);
        manageOwnerServicesUseCase = new ManageOwnerServicesUseCase(salonRepository, nailServiceRepository);
        manageOwnerEmployeesUseCase = new ManageOwnerEmployeesUseCase(salonRepository, employeeRepository);
        getOwnerBookingsUseCase = new GetOwnerBookingsUseCase(salonRepository, bookingRepository,
                new BookingResponseMapper(userRepository, salonRepository, nailServiceRepository));

        sampleSalon = new Salon(salonId, ownerId, "Beauty Nails", "Best salon in town", "123 Street",
                "Hanoi", "Ba Dinh", "0901234567", "owner@salon.com", "https://img.com/logo.png",
                SalonStatus.ACTIVE, 21.0, 105.0, "PRO", Instant.now(), Instant.now().plusSeconds(86400 * 30),
                BigDecimal.valueOf(4.8), 25, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("GetOwnerSalon: Success returns salon details")
    void getOwnerSalon_success() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));

        SalonDetailsResponse response = getOwnerSalonUseCase.execute(ownerId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(salonId);
        assertThat(response.name()).isEqualTo("Beauty Nails");
    }

    @Test
    @DisplayName("GetOwnerSalon: Throws exception when owner has no salon")
    void getOwnerSalon_notFound_throwsException() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of());

        assertThatThrownBy(() -> getOwnerSalonUseCase.execute(ownerId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Không tìm thấy salon của chủ tiệm");
    }

    @Test
    @DisplayName("UpdateOwnerSalon: Success updates salon profile fields")
    void updateOwnerSalon_success() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));
        when(salonRepository.save(any(Salon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOwnerSalonRequest request = new UpdateOwnerSalonRequest(
                "Updated Name", "Updated Desc", "456 New St", "Hanoi", "Cau Giay",
                "0987654321", "new@salon.com", "https://img.com/new.png"
        );

        SalonDetailsResponse response = updateOwnerSalonUseCase.execute(ownerId, request);

        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.address()).isEqualTo("456 New St");
        assertThat(response.district()).isEqualTo("Cau Giay");
    }

    @Test
    @DisplayName("GetOwnerDashboard: Aggregates metrics accurately")
    void getOwnerDashboard_success() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));
        when(bookingRepository.countBySalonId(salonId)).thenReturn(50L);
        when(bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.PENDING)).thenReturn(5L);
        when(bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.CONFIRMED)).thenReturn(15L);
        when(bookingRepository.countBySalonIdAndStatus(salonId, BookingStatus.COMPLETED)).thenReturn(30L);
        when(bookingRepository.sumCompletedGrossBySalonIdAndPeriod(any(), any(), any())).thenReturn(BigDecimal.valueOf(15000000));
        when(bookingRepository.countDistinctCompletedCustomersBySalonIdAndPeriod(any(), any(), any())).thenReturn(22L);
        when(reviewRepository.calculateAverageRatingBySalonId(salonId)).thenReturn(4.9);

        OwnerDashboardSummaryResponse response = getOwnerDashboardUseCase.execute(ownerId);

        assertThat(response.totalBookings()).isEqualTo(50L);
        assertThat(response.pendingBookings()).isEqualTo(5L);
        assertThat(response.confirmedBookings()).isEqualTo(15L);
        assertThat(response.completedBookings()).isEqualTo(30L);
        assertThat(response.monthlyRevenue()).isEqualByComparingTo(BigDecimal.valueOf(15000000));
        assertThat(response.monthlyCustomers()).isEqualTo(22L);
        assertThat(response.averageRating()).isEqualTo(4.9);
    }

    @Test
    @DisplayName("ManageServices: Create and retrieve services for salon")
    void manageServices_createAndGet() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));

        NailService service = NailService.createNew(salonId, 1L, "Gel Manicure", "Nice gel", BigDecimal.valueOf(150000), 45, null);
        service.setId(100L);
        when(nailServiceRepository.save(any(NailService.class))).thenReturn(service);
        when(nailServiceRepository.findBySalonId(salonId)).thenReturn(List.of(service));

        CreateServiceRequest request = new CreateServiceRequest(1L, "Gel Manicure", "Nice gel", BigDecimal.valueOf(150000), 45, null);
        ServiceResponse created = manageOwnerServicesUseCase.createService(ownerId, request);

        assertThat(created.id()).isEqualTo(100L);
        assertThat(created.name()).isEqualTo("Gel Manicure");

        List<ServiceResponse> list = manageOwnerServicesUseCase.getServices(ownerId);
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("ManageServices: Delete service enforces salon ownership")
    void manageServices_deleteForbiddenOnOtherSalon() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));

        NailService otherSalonService = NailService.createNew(999L, 1L, "Other Service", null, BigDecimal.valueOf(100000), 30, null);
        otherSalonService.setId(200L);
        when(nailServiceRepository.findById(200L)).thenReturn(Optional.of(otherSalonService));

        assertThatThrownBy(() -> manageOwnerServicesUseCase.deleteService(ownerId, 200L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Dịch vụ không thuộc salon của bạn");
    }

    @Test
    @DisplayName("ManageEmployees: Create, retrieve, and delete employee")
    void manageEmployees_lifecycle() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));

        Employee employee = Employee.createNew(salonId, "Le Thi B", "0912345678", null, "Senior technician");
        employee.setId(50L);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeRepository.findBySalonId(salonId)).thenReturn(List.of(employee));
        when(employeeRepository.findById(50L)).thenReturn(Optional.of(employee));

        CreateEmployeeRequest request = new CreateEmployeeRequest("Le Thi B", "0912345678", null, "Senior technician");
        EmployeeResponse created = manageOwnerEmployeesUseCase.createEmployee(ownerId, request);
        assertThat(created.fullName()).isEqualTo("Le Thi B");

        List<EmployeeResponse> list = manageOwnerEmployeesUseCase.getEmployees(ownerId);
        assertThat(list).hasSize(1);

        manageOwnerEmployeesUseCase.deleteEmployee(ownerId, 50L);
        assertThat(employee.isActive()).isFalse();
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("GetOwnerBookings: Returns salon bookings list")
    void getOwnerBookings_success() {
        when(salonRepository.findByOwnerId(ownerId)).thenReturn(List.of(sampleSalon));

        Booking booking = new Booking();
        booking.setId(300L);
        booking.setSalonId(salonId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setScheduledAt(Instant.now());
        booking.setDurationMinutes(60);

        when(bookingRepository.findBySalonId(salonId)).thenReturn(List.of(booking));

        List<BookingDetailsResponse> responses = getOwnerBookingsUseCase.execute(ownerId, null);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(300L);
    }
}
