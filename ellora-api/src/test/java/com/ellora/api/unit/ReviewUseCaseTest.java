package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.review.CreateReviewUseCase;
import com.ellora.application.feature.review.GetSalonReviewsUseCase;
import com.ellora.application.feature.review.ReplyToReviewUseCase;
import com.ellora.application.feature.review.dto.CreateReviewRequest;
import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.application.feature.review.dto.ReviewReplyRequest;
import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Booking;
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
class ReviewUseCaseTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SalonRepository salonRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NailServiceRepository nailServiceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private CreateReviewUseCase createReviewUseCase;
    private ReplyToReviewUseCase replyToReviewUseCase;
    private GetSalonReviewsUseCase getSalonReviewsUseCase;

    @BeforeEach
    void setUp() {
        createReviewUseCase = new CreateReviewUseCase(
                reviewRepository, bookingRepository, salonRepository, userRepository,
                nailServiceRepository, employeeRepository
        );
        replyToReviewUseCase = new ReplyToReviewUseCase(
                reviewRepository, salonRepository, userRepository, bookingRepository,
                nailServiceRepository, employeeRepository
        );
        getSalonReviewsUseCase = new GetSalonReviewsUseCase(
                reviewRepository, salonRepository, userRepository, bookingRepository,
                nailServiceRepository, employeeRepository
        );
    }

    @Test
    @DisplayName("CreateReview: Success when booking is completed and owned by user")
    void createReview_success() {
        UUID customerId = UUID.randomUUID();
        Long bookingId = 100L;
        Long salonId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomerId(customerId);
        booking.setSalonId(salonId);
        booking.setStatus(BookingStatus.COMPLETED);

        Salon salon = new Salon(salonId, UUID.randomUUID(), "Test Salon", null, "Address", "City", "District",
                "0123456789", "salon@test.com", null, SalonStatus.ACTIVE, null, null, null, null, null,
                BigDecimal.ZERO, 0, Instant.now(), Instant.now());

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));

        Review savedReview = new Review(1L, customerId, salonId, bookingId, 5, "Great service!", null, null, Instant.now(), Instant.now());
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.calculateAverageRatingBySalonId(salonId)).thenReturn(5.0);
        when(reviewRepository.countBySalonId(salonId)).thenReturn(1L);

        User customer = new User();
        customer.setId(customerId);
        customer.setFullName("Nguyen Van A");
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CreateReviewRequest request = new CreateReviewRequest(bookingId, 5, "Great service!");
        ReviewDetailsResponse response = createReviewUseCase.execute(customerId, request);

        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.customerName()).isEqualTo("Nguyen Van A");
        verify(salonRepository).save(any(Salon.class));
    }

    @Test
    @DisplayName("CreateReview: Fails if booking is not COMPLETED")
    void createReview_notCompleted_throwsException() {
        UUID customerId = UUID.randomUUID();
        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        CreateReviewRequest request = new CreateReviewRequest(bookingId, 5, "Nice");
        assertThatThrownBy(() -> createReviewUseCase.execute(customerId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Chỉ có thể đánh giá lịch hẹn đã hoàn thành");
    }

    @Test
    @DisplayName("CreateReview: Fails if booking is already reviewed")
    void createReview_alreadyReviewed_throwsException() {
        UUID customerId = UUID.randomUUID();
        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(true);

        CreateReviewRequest request = new CreateReviewRequest(bookingId, 5, "Nice");
        assertThatThrownBy(() -> createReviewUseCase.execute(customerId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Lịch hẹn này đã được đánh giá trước đó");
    }

    @Test
    @DisplayName("ReplyToReview: Success when user is salon owner")
    void replyToReview_success() {
        UUID ownerId = UUID.randomUUID();
        Long reviewId = 1L;
        Long salonId = 10L;

        Review review = new Review(reviewId, UUID.randomUUID(), salonId, 20L, 5, "Excellent!", null, null, Instant.now(), Instant.now());
        Salon salon = new Salon(salonId, ownerId, "My Salon", null, "Address", "City", "District",
                "0123456789", "salon@test.com", null, SalonStatus.ACTIVE, null, null, null, null, null,
                BigDecimal.ZERO, 0, Instant.now(), Instant.now());

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewReplyRequest request = new ReviewReplyRequest("Thank you so much!");
        ReviewDetailsResponse response = replyToReviewUseCase.execute(ownerId, reviewId, request);

        assertThat(response).isNotNull();
        assertThat(response.salonReply()).isEqualTo("Thank you so much!");
        assertThat(response.salonRepliedAt()).isNotNull();
    }

    @Test
    @DisplayName("ReplyToReview: Fails when user is not salon owner")
    void replyToReview_notOwner_throwsForbidden() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Long reviewId = 1L;
        Long salonId = 10L;

        Review review = new Review(reviewId, UUID.randomUUID(), salonId, 20L, 5, "Excellent!", null, null, Instant.now(), Instant.now());
        Salon salon = new Salon(salonId, ownerId, "My Salon", null, "Address", "City", "District",
                "0123456789", "salon@test.com", null, SalonStatus.ACTIVE, null, null, null, null, null,
                BigDecimal.ZERO, 0, Instant.now(), Instant.now());

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));

        ReviewReplyRequest request = new ReviewReplyRequest("Unauthorized reply");
        assertThatThrownBy(() -> replyToReviewUseCase.execute(otherUserId, reviewId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Bạn không có quyền phản hồi");
    }

    @Test
    @DisplayName("GetSalonReviews: Returns paginated reviews")
    void getSalonReviews_success() {
        Long salonId = 1L;
        Salon salon = new Salon();
        salon.setId(salonId);

        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));

        UUID customerId = UUID.randomUUID();
        Review review1 = new Review(1L, customerId, salonId, 10L, 5, "Good", null, null, Instant.now(), Instant.now());
        when(reviewRepository.findBySalonId(eq(salonId), eq(0), eq(10))).thenReturn(List.of(review1));
        when(reviewRepository.countBySalonId(salonId)).thenReturn(1L);

        PagedResult<ReviewDetailsResponse> result = getSalonReviewsUseCase.execute(salonId, 0, 10);

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.items().get(0).rating()).isEqualTo(5);
    }
}
