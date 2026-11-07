package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.SupportContact;
import com.ellora.domain.repository.SupportContactRepository;
import com.ellora.infrastructure.persistence.entity.SupportContactEntity;
import com.ellora.infrastructure.persistence.repository.SpringDataSupportContactRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupportContactRepositoryAdapter implements SupportContactRepository {

    private final SpringDataSupportContactRepository repository;

    private SupportContact toDomain(SupportContactEntity entity) {
        return new SupportContact(
                entity.getId(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getFacebookUrl()
        );
    }

    @Override
    public Optional<SupportContact> get() {
        return repository.findById(1L).map(this::toDomain);
    }

    @Override
    public SupportContact save(SupportContact contact) {
        SupportContactEntity entity = new SupportContactEntity();
        entity.setId(1L);
        entity.setEmail(contact.email() != null ? contact.email().trim() : "");
        entity.setPhone(contact.phone() != null ? contact.phone().trim() : "");
        entity.setFacebookUrl(contact.facebookUrl() != null ? contact.facebookUrl().trim() : "");
        return toDomain(repository.save(entity));
    }
}
