package com.ellora.application.feature.owner;

import com.ellora.application.feature.admin.dto.CreateSalonChangeRequest;
import com.ellora.application.feature.admin.dto.SalonChangeFieldsDto;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.SalonChange;
import com.ellora.domain.repository.SalonChangeRepository;
import com.ellora.domain.repository.SalonRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmitSalonChangeUseCase {

    private final SalonChangeRepository salonChangeRepository;
    private final SalonRepository salonRepository;

    @Transactional
    public void execute(UUID ownerId, CreateSalonChangeRequest request) {
        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }
        Salon salon = salons.get(0);

        String kind = request.kind() != null ? request.kind().toUpperCase() : "PROFILE";
        String proposedJson = serialize(request.proposed());

        SalonChange change = new SalonChange(
                null,
                salon.getId(),
                kind,
                proposedJson,
                salon.getLogoUrl(),
                request.proposedLogoUrl(),
                null,
                null,
                "PENDING",
                null,
                null,
                Instant.now()
        );

        salonChangeRepository.save(change);
    }

    private String serialize(SalonChangeFieldsDto dto) {
        if (dto == null) return "{}";
        StringBuilder sb = new StringBuilder("{");
        append(sb, "name", dto.name());
        append(sb, "description", dto.description());
        append(sb, "address", dto.address());
        append(sb, "city", dto.city());
        append(sb, "district", dto.district());
        append(sb, "phone", dto.phone());
        append(sb, "email", dto.email());
        if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private void append(StringBuilder sb, String key, String val) {
        if (val != null) {
            sb.append("\"").append(key).append("\":\"")
                    .append(val.replace("\"", "\\\"")).append("\",");
        }
    }
}
