package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.CreateSalonClosureRequest;
import com.ellora.application.feature.owner.dto.SalonClosureDto;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.SalonClosure;
import com.ellora.domain.repository.SalonClosureRepository;
import com.ellora.domain.repository.SalonRepository;
import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalonClosureUseCase {
    private final SalonRepository salons;
    private final SalonClosureRepository closures;

    @Transactional(readOnly = true)
    public List<SalonClosureDto> forSalon(Long salonId) {
        if (salons.findById(salonId).isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon");
        }
        return closures.findBySalonId(salonId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LocalDate> datesForSalon(Long salonId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BusinessRuleException("INVALID_DATE_RANGE", "Khoảng ngày không hợp lệ");
        }
        return forSalon(salonId).stream().map(SalonClosureDto::date)
                .filter(date -> !date.isBefore(from) && !date.isAfter(to)).filter(Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public List<SalonClosureDto> forOwner(UUID ownerId) {
        return forSalon(ownerSalonId(ownerId));
    }

    @Transactional
    public SalonClosureDto create(UUID ownerId, CreateSalonClosureRequest request) {
        Long salonId = ownerSalonId(ownerId);
        if (request == null || request.date() == null) {
            throw new BusinessRuleException("INVALID_CLOSURE_DATE", "Ngày nghỉ là bắt buộc");
        }
        if (closures.findBySalonIdAndDate(salonId, request.date()).isPresent()) {
            throw new BusinessRuleException("CLOSURE_ALREADY_EXISTS", "Ngày này đã được đánh dấu nghỉ");
        }
        SalonClosure saved = closures.save(new SalonClosure(null, salonId, request.date(), normalizeReason(request.reason())));
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID ownerId, Long id) {
        Long salonId = ownerSalonId(ownerId);
        SalonClosure closure = closures.findBySalonIdAndId(salonId, id).orElseThrow(
                () -> new BusinessRuleException("CLOSURE_NOT_FOUND", "Không tìm thấy ngày nghỉ"));
        closures.deleteById(closure.id());
    }

    private Long ownerSalonId(UUID ownerId) {
        if (ownerId == null) throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        return salons.findByOwnerId(ownerId).stream().findFirst().orElseThrow(
                () -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon")).getId();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) return null;
        String value = reason.trim();
        if (value.length() > 500) throw new BusinessRuleException("INVALID_CLOSURE_REASON", "Lý do nghỉ tối đa 500 ký tự");
        return value;
    }

    private SalonClosureDto toDto(SalonClosure closure) {
        return new SalonClosureDto(closure.id(), closure.date(), closure.reason());
    }
}
