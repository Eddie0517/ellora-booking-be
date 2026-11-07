package com.ellora.application.feature.admin;

import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.SalonChange;
import com.ellora.domain.repository.SalonChangeRepository;
import com.ellora.domain.repository.SalonRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecideSalonChangeUseCase {

    private final SalonChangeRepository salonChangeRepository;
    private final SalonRepository salonRepository;

    @Transactional
    public void execute(Long changeId, String action, UUID adminId) {
        SalonChange change = salonChangeRepository.findById(changeId)
                .orElseThrow(() -> new BusinessRuleException("CHANGE_NOT_FOUND", "Không tìm thấy yêu cầu thay đổi thông tin"));

        if (!"PENDING".equalsIgnoreCase(change.status())) {
            throw new BusinessRuleException("ALREADY_PROCESSED", "Yêu cầu này đã được xử lý trước đó");
        }

        boolean approved = "approve".equalsIgnoreCase(action);
        String newStatus = approved ? "APPROVED" : "REJECTED";

        if (approved) {
            Salon salon = salonRepository.findById(change.salonId())
                    .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

            if ("PROFILE".equalsIgnoreCase(change.kind()) && change.proposedJson() != null) {
                String name = extract(change.proposedJson(), "name");
                String desc = extract(change.proposedJson(), "description");
                String addr = extract(change.proposedJson(), "address");
                String city = extract(change.proposedJson(), "city");
                String dist = extract(change.proposedJson(), "district");
                String phone = extract(change.proposedJson(), "phone");
                String email = extract(change.proposedJson(), "email");

                if (name != null) salon.setName(name);
                if (desc != null) salon.setDescription(desc);
                if (addr != null) salon.setAddress(addr);
                if (city != null) salon.setCity(city);
                if (dist != null) salon.setDistrict(dist);
                if (phone != null) salon.setPhone(phone);
                if (email != null) salon.setEmail(email);
            } else if ("COVER".equalsIgnoreCase(change.kind()) || "GALLERY".equalsIgnoreCase(change.kind())) {
                if (change.proposedLogoUrl() != null) {
                    salon.setLogoUrl(change.proposedLogoUrl());
                }
            }

            salonRepository.save(salon);
        }

        SalonChange updated = new SalonChange(
                change.id(),
                change.salonId(),
                change.kind(),
                change.proposedJson(),
                change.previousLogoUrl(),
                change.proposedLogoUrl(),
                change.previousImageUrlsJson(),
                change.proposedImageUrlsJson(),
                newStatus,
                Instant.now(),
                adminId,
                change.submittedAt()
        );
        salonChangeRepository.save(updated);
    }

    private String extract(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int start = idx + pattern.length();
        int end = json.indexOf("\"", start);
        return end != -1 ? json.substring(start, end) : null;
    }
}
