package com.ellora.application.feature.support;

import com.ellora.application.feature.support.dto.SupportContactResponse;
import com.ellora.domain.repository.SupportContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSupportContactUseCase {

    private final SupportContactRepository supportContactRepository;

    public SupportContactResponse execute() {
        return supportContactRepository.get()
                .map(c -> new SupportContactResponse(c.email(), c.phone(), c.facebookUrl()))
                .orElseGet(() -> new SupportContactResponse("hotro@ellora.vn", "0900000000", "https://facebook.com/elloravietnam"));
    }
}
