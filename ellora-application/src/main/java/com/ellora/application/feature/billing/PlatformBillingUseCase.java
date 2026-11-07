package com.ellora.application.feature.billing;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.common.port.MailSender;
import com.ellora.application.feature.billing.dto.BillingConfigResponse;
import com.ellora.application.feature.billing.dto.PlatformBillResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.BookingRepository;
import com.ellora.domain.repository.PlatformBillingSettingsRepository;
import com.ellora.domain.repository.PlatformFeePolicyRepository;
import com.ellora.domain.repository.PlatformStatementRepository;
import com.ellora.domain.repository.SalonRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformBillingUseCase {

    private final PlatformFeePolicyRepository feePolicyRepository;
    private final PlatformBillingSettingsRepository billingSettingsRepository;
    private final PlatformStatementRepository statementRepository;
    private final SalonRepository salonRepository;
    private final BookingRepository bookingRepository;
    private final MailSender mailSender;

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private LocalDate currentMonth() {
        return LocalDate.now(ZONE).withDayOfMonth(1);
    }

    private LocalDate parseMonth(String monthStr) {
        try {
            LocalDate month = YearMonth.parse(monthStr).atDay(1);
            if (month.isAfter(currentMonth()) || month.isBefore(currentMonth().minusYears(10))) {
                throw new BusinessRuleException("INVALID_MONTH", "Tháng không hợp lệ, không lớn hơn tháng hiện tại (tối đa 10 năm)");
            }
            return month;
        } catch (Exception e) {
            throw new BusinessRuleException("INVALID_MONTH", "Định dạng tháng không hợp lệ (YYYY-MM)");
        }
    }

    private BigDecimal getRate(LocalDate month) {
        return feePolicyRepository.findRateForMonth(month).orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public BillingConfigResponse getConfiguration() {
        LocalDate current = currentMonth();
        LocalDate next = current.plusMonths(1);
        BigDecimal currentPercent = getRate(current);
        BigDecimal nextPercent = feePolicyRepository.findRateForMonth(next).orElse(currentPercent);
        String qrUrl = billingSettingsRepository.findQrUrl().orElse(null);
        return new BillingConfigResponse(currentPercent, nextPercent, next, qrUrl);
    }

    @Transactional
    public BillingConfigResponse setRate(BigDecimal percent) {
        LocalDate next = currentMonth().plusMonths(1);
        feePolicyRepository.saveRate(next, percent);
        return getConfiguration();
    }

    @Transactional(readOnly = true)
    public PlatformBillResponse getOwnerBill(UUID ownerId, String monthStr) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }
        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }
        return computeBill(salons.get(0), parseMonth(monthStr));
    }

    @Transactional(readOnly = true)
    public PagedResult<PlatformBillResponse> listStatements(String monthStr, int page, int size) {
        LocalDate month = parseMonth(monthStr);
        List<Salon> all = salonRepository.findAll();
        long total = all.size();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());

        List<PlatformBillResponse> items = all.subList(from, to).stream()
                .map(s -> computeBill(s, month))
                .toList();

        return PagedResult.of(items, page, size, total);
    }

    @Transactional
    public PlatformBillResponse confirmPayment(Long salonId, String monthStr, BigDecimal expectedDue, UUID adminId) {
        LocalDate month = parseMonth(monthStr);
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        PlatformBillResponse currentBill = computeBill(salon, month);
        if (currentBill.due().compareTo(BigDecimal.ZERO) == 0) {
            return currentBill;
        }
        if (expectedDue != null && currentBill.due().compareTo(expectedDue) != 0) {
            throw new BusinessRuleException("AMOUNT_MISMATCH", "Số tiền nợ đã thay đổi. Vui lòng tải lại trước khi xác nhận");
        }

        statementRepository.recordPayment(salonId, month, currentBill.due(), adminId);
        return computeBill(salon, month);
    }

    @Transactional
    public void remindPayment(Long salonId, String monthStr) {
        LocalDate month = parseMonth(monthStr);
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        PlatformBillResponse bill = computeBill(salon, month);
        if (bill.due().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessRuleException("NO_DUE", "Tiệm không còn phí phải thanh toán trong tháng này");
        }

        Optional<PlatformStatementRepository.StatementRecord> existing = statementRepository.findBySalonIdAndMonth(salonId, month);
        if (existing.isPresent() && existing.get().remindedAt() != null) {
            if (existing.get().remindedAt().isAfter(Instant.now().minusSeconds(300))) {
                throw new BusinessRuleException("TOO_FREQUENT", "Đã gửi nhắc thanh toán gần đây. Vui lòng đợi 5 phút");
            }
        }

        statementRepository.recordReminder(salonId, month);

        if (salon.getEmail() != null && !salon.getEmail().isBlank()) {
            mailSender.sendText(
                    salon.getEmail(),
                    "Nhắc nhở thanh toán phí nền tảng tháng " + bill.month(),
                    "Kính gửi " + salon.getName() + ",\n\nBạn có khoản phí nền tảng tháng " + bill.month()
                            + " cần thanh toán: " + bill.due() + " ₫.\nHạn thanh toán: " + bill.dueDate()
                            + ".\nNội dung chuyển khoản: " + bill.transferContent() + "\n\nTrân trọng,\nĐội ngũ Ellora."
            );
        }
    }

    private PlatformBillResponse computeBill(Salon salon, LocalDate month) {
        Instant start = month.atStartOfDay(ZONE).toInstant();
        Instant end = month.plusMonths(1).atStartOfDay(ZONE).toInstant();

        BigDecimal gross = bookingRepository.sumCompletedGrossBySalonIdAndPeriod(salon.getId(), start, end);
        if (gross == null) gross = BigDecimal.ZERO;

        BigDecimal percent = getRate(month);
        BigDecimal fee = gross.multiply(percent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        Optional<PlatformStatementRepository.StatementRecord> stmt = statementRepository.findBySalonIdAndMonth(salon.getId(), month);
        BigDecimal paid = stmt.map(PlatformStatementRepository.StatementRecord::paidAmount).orElse(BigDecimal.ZERO);
        BigDecimal due = fee.subtract(paid).max(BigDecimal.ZERO);

        LocalDate deadline = month.withDayOfMonth(month.lengthOfMonth());
        String status = due.compareTo(BigDecimal.ZERO) == 0
                ? (fee.compareTo(BigDecimal.ZERO) == 0 ? "NO_FEE" : "PAID")
                : (LocalDate.now(ZONE).isAfter(deadline) ? "OVERDUE" : "UNPAID");

        String ym = YearMonth.from(month).toString();
        String transferCode = "ELLORA " + salon.getId() + " " + ym.replace("-", "");
        String qrUrl = billingSettingsRepository.findQrUrl().orElse(null);

        return new PlatformBillResponse(
                salon.getId(),
                salon.getName(),
                ym,
                gross,
                percent,
                fee,
                paid,
                due,
                deadline,
                status,
                transferCode,
                qrUrl,
                stmt.map(PlatformStatementRepository.StatementRecord::paidAt).orElse(null),
                stmt.map(PlatformStatementRepository.StatementRecord::remindedAt).orElse(null)
        );
    }
}
