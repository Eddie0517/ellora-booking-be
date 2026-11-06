package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.SalonClosure;
import com.ellora.domain.repository.SalonClosureRepository;
import com.ellora.infrastructure.persistence.entity.SalonClosureEntity;
import com.ellora.infrastructure.persistence.repository.SpringDataSalonClosureRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalonClosureRepositoryAdapter implements SalonClosureRepository {
    private final SpringDataSalonClosureRepository repository;

    private SalonClosure toDomain(SalonClosureEntity entity) {
        return new SalonClosure(entity.getId(), entity.getSalonId(), entity.getDate(), entity.getReason());
    }

    @Override public List<SalonClosure> findBySalonId(Long salonId) {
        return repository.findBySalonIdOrderByDateAsc(salonId).stream().map(this::toDomain).toList();
    }
    @Override public Optional<SalonClosure> findBySalonIdAndDate(Long salonId, LocalDate date) {
        return repository.findBySalonIdAndDate(salonId, date).map(this::toDomain);
    }
    @Override public Optional<SalonClosure> findBySalonIdAndId(Long salonId, Long id) {
        return repository.findBySalonIdAndId(salonId, id).map(this::toDomain);
    }
    @Override public SalonClosure save(SalonClosure closure) {
        var entity = new SalonClosureEntity();
        entity.setSalonId(closure.salonId());
        entity.setDate(closure.date());
        entity.setReason(closure.reason());
        return toDomain(repository.save(entity));
    }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
}
