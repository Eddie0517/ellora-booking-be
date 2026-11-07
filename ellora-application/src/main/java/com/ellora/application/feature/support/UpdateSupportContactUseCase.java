package com.ellora.application.feature.support;

import com.ellora.application.feature.support.dto.SupportContactResponse;
import com.ellora.application.feature.support.dto.UpdateSupportContactRequest;
import com.ellora.domain.model.SupportContact;
import com.ellora.domain.repository.SupportContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSupportContactUseCase {

    private final SupportContactRepository supportContactRepository;

    @Transactional
    public SupportContactResponse execute(UpdateSupportContactRequest request) {
        SupportContact saved = supportContactRepository.save(new SupportContact(
                1L,
                request.email(),
                request.phone(),
                request.facebookUrl()
        ));
        return new SupportContactResponse(saved.email(), saved.phone(), saved.facebookUrl());
    }
}
