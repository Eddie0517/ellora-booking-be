package com.ellora.domain.model;

import com.ellora.domain.constant.BookingStatus;
import com.ellora.domain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an appointment booking.
 * Encapsulates lifecycle state transitions and domain business rules.
 */
public class Booking {

    private Long id;
    private UUID customerId;
    private String walkInName;
    private String guestEmail;
    private String walkInPhone;
    private Long salonId;
    private Long serviceId;
    private Long employeeId;
    private Instant scheduledAt;
    private Integer durationMinutes;
    private BigDecimal servicePriceSnapshot;
    private BookingStatus status;
    private String customerNote;
    private String salonNote;
    private String cancellationReason;
    private Instant completedAt;
    private String completionConfirmation;
    private Long completionVersion;
    private Instant completionRespondedAt;
    private String completionCustomerNote;
    private String completionAdminNote;
    private Instant completionResolvedAt;
    private boolean revenueEstimated;
    private Instant createdAt;
    private Instant updatedAt;

    public Booking() {}

    public Booking(
            Long id,
            UUID customerId,
            String walkInName,
            String guestEmail,
            String walkInPhone,
            Long salonId,
            Long serviceId,
            Long employeeId,
            Instant scheduledAt,
            Integer durationMinutes,
            BigDecimal servicePriceSnapshot,
            BookingStatus status,
            String customerNote,
            String salonNote,
            String cancellationReason,
            Instant completedAt,
            String completionConfirmation,
            Long completionVersion,
            Instant completionRespondedAt,
            String completionCustomerNote,
            String completionAdminNote,
            Instant completionResolvedAt,
            boolean revenueEstimated,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.walkInName = walkInName;
        this.guestEmail = guestEmail;
        this.walkInPhone = walkInPhone;
        this.salonId = salonId;
        this.serviceId = serviceId;
        this.employeeId = employeeId;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes != null ? durationMinutes : 30;
        this.servicePriceSnapshot = servicePriceSnapshot != null ? servicePriceSnapshot : BigDecimal.ZERO;
        this.status = status != null ? status : BookingStatus.PENDING;
        this.customerNote = customerNote;
        this.salonNote = salonNote;
        this.cancellationReason = cancellationReason;
        this.completedAt = completedAt;
        this.completionConfirmation = completionConfirmation;
        this.completionVersion = completionVersion != null ? completionVersion : 0L;
        this.completionRespondedAt = completionRespondedAt;
        this.completionCustomerNote = completionCustomerNote;
        this.completionAdminNote = completionAdminNote;
        this.completionResolvedAt = completionResolvedAt;
        this.revenueEstimated = revenueEstimated;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Booking createNew(
            UUID customerId,
            Long salonId,
            Long serviceId,
            Long employeeId,
            Instant scheduledAt,
            Integer durationMinutes,
            BigDecimal servicePriceSnapshot,
            String customerNote) {
        if (customerId == null) {
            throw new BusinessRuleException("INVALID_CUSTOMER", "Mã khách hàng là bắt buộc đối với khách hàng đã đăng ký");
        }
        if (salonId == null) {
            throw new BusinessRuleException("INVALID_SALON", "Mã salon là bắt buộc khi đặt lịch");
        }
        if (serviceId == null) {
            throw new BusinessRuleException("INVALID_SERVICE", "Mã dịch vụ là bắt buộc khi đặt lịch");
        }
        if (scheduledAt == null) {
            throw new BusinessRuleException("INVALID_SCHEDULE", "Thời gian đặt lịch không được để trống");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new BusinessRuleException("INVALID_DURATION", "Thời lượng dịch vụ phải lớn hơn 0 phút");
        }
        if (servicePriceSnapshot == null || servicePriceSnapshot.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("INVALID_PRICE", "Giá dịch vụ không hợp lệ");
        }

        Instant now = Instant.now();
        return new Booking(
                null,
                customerId,
                null,
                null,
                null,
                salonId,
                serviceId,
                employeeId,
                scheduledAt,
                durationMinutes,
                servicePriceSnapshot,
                BookingStatus.PENDING,
                customerNote,
                null,
                null,
                null,
                null,
                0L,
                null,
                null,
                null,
                null,
                false,
                now,
                now
        );
    }

    public static Booking createGuest(
            String walkInName,
            String guestEmail,
            String walkInPhone,
            Long salonId,
            Long serviceId,
            Long employeeId,
            Instant scheduledAt,
            Integer durationMinutes,
            BigDecimal servicePriceSnapshot,
            String customerNote) {
        if (walkInName == null || walkInName.trim().isEmpty()) {
            throw new BusinessRuleException("INVALID_GUEST_NAME", "Tên khách vãng lai không được để trống");
        }
        if ((walkInPhone == null || walkInPhone.trim().isEmpty()) && (guestEmail == null || guestEmail.trim().isEmpty())) {
            throw new BusinessRuleException("INVALID_GUEST_CONTACT", "Cần cung cấp ít nhất số điện thoại hoặc email liên hệ");
        }
        if (salonId == null) {
            throw new BusinessRuleException("INVALID_SALON", "Mã salon là bắt buộc khi đặt lịch");
        }
        if (serviceId == null) {
            throw new BusinessRuleException("INVALID_SERVICE", "Mã dịch vụ là bắt buộc khi đặt lịch");
        }
        if (scheduledAt == null) {
            throw new BusinessRuleException("INVALID_SCHEDULE", "Thời gian đặt lịch không được để trống");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new BusinessRuleException("INVALID_DURATION", "Thời lượng dịch vụ phải lớn hơn 0 phút");
        }
        if (servicePriceSnapshot == null || servicePriceSnapshot.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("INVALID_PRICE", "Giá dịch vụ không hợp lệ");
        }

        Instant now = Instant.now();
        return new Booking(
                null,
                null,
                walkInName.trim(),
                guestEmail != null ? guestEmail.trim() : null,
                walkInPhone != null ? walkInPhone.trim() : null,
                salonId,
                serviceId,
                employeeId,
                scheduledAt,
                durationMinutes,
                servicePriceSnapshot,
                BookingStatus.PENDING,
                customerNote,
                null,
                null,
                null,
                null,
                0L,
                null,
                null,
                null,
                null,
                false,
                now,
                now
        );
    }

    public void confirm() {
        if (this.status != BookingStatus.PENDING) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Chỉ có thể xác nhận lịch hẹn ở trạng thái chờ (PENDING). Trạng thái hiện tại: " + this.status);
        }
        this.status = BookingStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        complete("CUSTOMER_CONFIRMED");
    }

    public void complete(String confirmation) {
        if (this.status == BookingStatus.COMPLETED || this.status == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Không thể hoàn thành lịch hẹn ở trạng thái " + this.status);
        }
        this.status = BookingStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.completionConfirmation = confirmation != null ? confirmation : "CUSTOMER_CONFIRMED";
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == BookingStatus.COMPLETED) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Không thể hủy lịch hẹn đã hoàn thành (COMPLETED)");
        }
        if (this.status == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Lịch hẹn đã bị hủy trước đó");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedAt = Instant.now();
    }

    public void dispute(String note) {
        if (this.status != BookingStatus.COMPLETED) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Chỉ có thể khiếu nại lịch hẹn đã hoàn thành. Trạng thái hiện tại: " + this.status);
        }
        this.status = BookingStatus.DISPUTED;
        this.completionCustomerNote = note;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getWalkInName() { return walkInName; }
    public void setWalkInName(String walkInName) { this.walkInName = walkInName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getWalkInPhone() { return walkInPhone; }
    public void setWalkInPhone(String walkInPhone) { this.walkInPhone = walkInPhone; }

    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getServicePriceSnapshot() { return servicePriceSnapshot; }
    public void setServicePriceSnapshot(BigDecimal servicePriceSnapshot) { this.servicePriceSnapshot = servicePriceSnapshot; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }

    public String getSalonNote() { return salonNote; }
    public void setSalonNote(String salonNote) { this.salonNote = salonNote; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getCompletionConfirmation() { return completionConfirmation; }
    public void setCompletionConfirmation(String completionConfirmation) { this.completionConfirmation = completionConfirmation; }

    public Long getCompletionVersion() { return completionVersion; }
    public void setCompletionVersion(Long completionVersion) { this.completionVersion = completionVersion; }

    public Instant getCompletionRespondedAt() { return completionRespondedAt; }
    public void setCompletionRespondedAt(Instant completionRespondedAt) { this.completionRespondedAt = completionRespondedAt; }

    public String getCompletionCustomerNote() { return completionCustomerNote; }
    public void setCompletionCustomerNote(String completionCustomerNote) { this.completionCustomerNote = completionCustomerNote; }

    public String getCompletionAdminNote() { return completionAdminNote; }
    public void setCompletionAdminNote(String completionAdminNote) { this.completionAdminNote = completionAdminNote; }

    public Instant getCompletionResolvedAt() { return completionResolvedAt; }
    public void setCompletionResolvedAt(Instant completionResolvedAt) { this.completionResolvedAt = completionResolvedAt; }

    public boolean isRevenueEstimated() { return revenueEstimated; }
    public void setRevenueEstimated(boolean revenueEstimated) { this.revenueEstimated = revenueEstimated; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
