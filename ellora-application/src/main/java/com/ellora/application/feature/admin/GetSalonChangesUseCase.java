package com.ellora.application.feature.admin;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.dto.SalonChangeFieldsDto;
import com.ellora.application.feature.admin.dto.SalonChangeResponse;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.SalonChange;
import com.ellora.domain.repository.SalonChangeRepository;
import com.ellora.domain.repository.SalonRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSalonChangesUseCase {

    private final SalonChangeRepository salonChangeRepository;
    private final SalonRepository salonRepository;

    public PagedResult<SalonChangeResponse> execute(int page, int size) {
        int targetPage = Math.max(page, 0);
        int targetSize = size > 0 ? size : 20;

        List<SalonChange> changes = salonChangeRepository.findPending(targetPage, targetSize);
        long totalItems = salonChangeRepository.countPending();
        int totalPages = (int) Math.ceil((double) totalItems / targetSize);

        List<SalonChangeResponse> responses = new ArrayList<>();
        for (SalonChange change : changes) {
            Optional<Salon> salonOpt = salonRepository.findById(change.salonId());
            String salonName = salonOpt.map(Salon::getName).orElse("Salon #" + change.salonId());

            SalonChangeFieldsDto current = salonOpt.map(s -> new SalonChangeFieldsDto(
                    s.getName(),
                    s.getDescription(),
                    s.getAddress(),
                    s.getCity(),
                    s.getDistrict(),
                    s.getPhone(),
                    s.getEmail(),
                    s.getLatitude(),
                    s.getLongitude()
            )).orElse(null);

            SalonChangeFieldsDto proposed = parseFields(change.proposedJson());

            responses.add(new SalonChangeResponse(
                    change.id(),
                    change.salonId(),
                    salonName,
                    change.kind(),
                    change.status(),
                    change.submittedAt(),
                    current,
                    proposed,
                    change.previousLogoUrl(),
                    change.proposedLogoUrl(),
                    List.of(),
                    List.of()
            ));
        }

        return new PagedResult<>(responses, targetPage + 1, targetSize, totalItems, totalPages);
    }

    private SalonChangeFieldsDto parseFields(String json) {
        if (json == null || json.isBlank()) return null;
        String name = extract(json, "name");
        String desc = extract(json, "description");
        String addr = extract(json, "address");
        String city = extract(json, "city");
        String dist = extract(json, "district");
        String phone = extract(json, "phone");
        String email = extract(json, "email");
        return new SalonChangeFieldsDto(name, desc, addr, city, dist, phone, email, null, null);
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
