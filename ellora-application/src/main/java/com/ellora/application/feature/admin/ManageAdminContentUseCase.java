package com.ellora.application.feature.admin;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.common.port.ImageStorage;
import com.ellora.application.feature.admin.dto.ContentDetailResponse;
import com.ellora.application.feature.admin.dto.ContentPhotoItem;
import com.ellora.application.feature.admin.dto.ContentSalonResponse;
import com.ellora.application.feature.review.GetSalonReviewsUseCase;
import com.ellora.application.feature.review.dto.ReviewDetailsResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Review;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.ReviewRepository;
import com.ellora.domain.repository.SalonRepository;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageAdminContentUseCase {

    private final SalonRepository salonRepository;
    private final NailServiceRepository nailServiceRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewRepository reviewRepository;
    private final GetSalonReviewsUseCase getSalonReviewsUseCase;
    private final ImageStorage imageStorage;

    @Transactional(readOnly = true)
    public PagedResult<ContentSalonResponse> listSalons(int page, int size) {
        List<Salon> all = salonRepository.findAll();
        long total = all.size();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<ContentSalonResponse> items = all.subList(from, to).stream()
                .map(s -> new ContentSalonResponse(s.getId(), s.getName(), s.getStatus() != null ? s.getStatus().name() : "ACTIVE"))
                .toList();
        return PagedResult.of(items, page, size, total);
    }

    @Transactional(readOnly = true)
    public ContentDetailResponse getSalonDetail(Long id) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        List<ContentPhotoItem> items = new ArrayList<>();
        items.add(new ContentPhotoItem("cover", salon.getId(), "Ảnh đại diện salon", salon.getLogoUrl()));

        List<NailService> services = nailServiceRepository.findBySalonId(id);
        for (NailService s : services) {
            items.add(new ContentPhotoItem("services", s.getId(), s.getName(), s.getImageUrl()));
        }

        List<Employee> employees = employeeRepository.findBySalonId(id);
        for (Employee e : employees) {
            items.add(new ContentPhotoItem("employees", e.getId(), e.getFullName(), e.getAvatarUrl()));
        }

        return new ContentDetailResponse(salon.getId(), salon.getName(), items, new ArrayList<>(salon.getImageUrls()));
    }

    @Transactional(readOnly = true)
    public PagedResult<ReviewDetailsResponse> getSalonReviews(Long salonId, int page, int size) {
        return getSalonReviewsUseCase.execute(salonId, page, size);
    }

    @Transactional
    public String uploadPhoto(Long salonId, String kind, Long id, String filename, InputStream in, long size, String contentType, String oldUrl) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        String url = imageStorage.uploadImage("ellora/" + kind, filename, in, size, contentType);

        switch (kind.toLowerCase()) {
            case "cover" -> {
                salon.setLogoUrl(url);
                salonRepository.save(salon);
                return url;
            }
            case "services" -> {
                NailService service = nailServiceRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
                service.setImageUrl(url);
                nailServiceRepository.save(service);
                return url;
            }
            case "employees" -> {
                Employee employee = employeeRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
                employee.setAvatarUrl(url);
                employeeRepository.save(employee);
                return url;
            }
            case "gallery" -> {
                List<String> gallery = new ArrayList<>(salon.getImageUrls());
                if (oldUrl != null && !oldUrl.isBlank()) {
                    int index = gallery.indexOf(oldUrl);
                    if (index >= 0) gallery.set(index, url);
                    else gallery.add(url);
                } else {
                    gallery.add(url);
                }
                salon.setImageUrls(gallery);
                salonRepository.save(salon);
                return url;
            }
            default -> throw new BusinessRuleException("INVALID_KIND", "Loại ảnh không hợp lệ: " + kind);
        }
    }

    @Transactional
    public void removePhoto(Long salonId, String kind, Long id, String url) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        switch (kind.toLowerCase()) {
            case "cover" -> {
                salon.setLogoUrl(null);
                salonRepository.save(salon);
            }
            case "services" -> {
                NailService service = nailServiceRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
                service.setImageUrl(null);
                nailServiceRepository.save(service);
            }
            case "employees" -> {
                Employee employee = employeeRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
                employee.setAvatarUrl(null);
                employeeRepository.save(employee);
            }
            case "gallery" -> {
                if (url != null) {
                    List<String> gallery = new ArrayList<>(salon.getImageUrls());
                    gallery.remove(url);
                    salon.setImageUrls(gallery);
                    salonRepository.save(salon);
                }
            }
            default -> throw new BusinessRuleException("INVALID_KIND", "Loại ảnh không hợp lệ: " + kind);
        }
    }

    @Transactional
    public void editReview(Long reviewId, int rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessRuleException("REVIEW_NOT_FOUND", "Không tìm thấy đánh giá"));
        review.adminEdit(rating, comment);
        reviewRepository.save(review);
        updateSalonRating(review.getSalonId());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessRuleException("REVIEW_NOT_FOUND", "Không tìm thấy đánh giá"));
        Long salonId = review.getSalonId();
        reviewRepository.delete(reviewId);
        updateSalonRating(salonId);
    }

    @Transactional
    public void replyReview(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessRuleException("REVIEW_NOT_FOUND", "Không tìm thấy đánh giá"));
        review.adminReply(reply);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReply(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessRuleException("REVIEW_NOT_FOUND", "Không tìm thấy đánh giá"));
        review.adminDeleteReply();
        reviewRepository.save(review);
    }

    private void updateSalonRating(Long salonId) {
        Double avg = reviewRepository.calculateAverageRatingBySalonId(salonId);
        long count = reviewRepository.countBySalonId(salonId);
        salonRepository.findById(salonId).ifPresent(s -> {
            s.updateRatingStats(avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO, (int) count);
            salonRepository.save(s);
        });
    }
}
