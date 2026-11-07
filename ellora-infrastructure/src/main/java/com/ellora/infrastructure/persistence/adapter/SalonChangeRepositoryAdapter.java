package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.SalonChange;
import com.ellora.domain.repository.SalonChangeRepository;
import com.ellora.infrastructure.persistence.entity.SalonChangeEntity;
import com.ellora.infrastructure.persistence.repository.SpringDataSalonChangeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalonChangeRepositoryAdapter implements SalonChangeRepository {

    private final SpringDataSalonChangeRepository repository;

    private SalonChange toDomain(SalonChangeEntity entity) {
        return new SalonChange(
                entity.getId(),
                entity.getSalonId(),
                entity.getKind(),
                entity.getProposedJson(),
                entity.getPreviousLogoUrl(),
                entity.getProposedLogoUrl(),
                entity.getPreviousImageUrlsJson(),
                entity.getProposedImageUrlsJson(),
                entity.getStatus(),
                entity.getReviewedAt(),
                entity.getReviewedBy(),
                entity.getSubmittedAt()
        );
    }

    private SalonChangeEntity toEntity(SalonChange domain) {
        SalonChangeEntity entity = new SalonChangeEntity();
        entity.setId(domain.id());
        entity.setSalonId(domain.salonId());
        entity.setKind(domain.kind());
        entity.setProposedJson(domain.proposedJson());
        entity.setPreviousLogoUrl(domain.previousLogoUrl());
        entity.setProposedLogoUrl(domain.proposedLogoUrl());
        entity.setPreviousImageUrlsJson(domain.previousImageUrlsJson());
        entity.setProposedImageUrlsJson(domain.proposedImageUrlsJson());
        entity.setStatus(domain.status() != null ? domain.status() : "PENDING");
        entity.setReviewedAt(domain.reviewedAt());
        entity.setReviewedBy(domain.reviewedBy());
        if (domain.submittedAt() != null) {
            entity.setSubmittedAt(domain.submittedAt());
        }
        return entity;
    }

    @Override
    public List<SalonChange> findPending(int page, int size) {
        return repository.findByStatusOrderBySubmittedAtDesc("PENDING", PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countPending() {
        return repository.countByStatus("PENDING");
    }

    @Override
    public Optional<SalonChange> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public SalonChange save(SalonChange change) {
        return toDomain(repository.save(toEntity(change)));
    }
}
