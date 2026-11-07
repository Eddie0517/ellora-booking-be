package com.ellora.domain.repository;

import com.ellora.domain.model.SalonChange;
import java.util.List;
import java.util.Optional;

public interface SalonChangeRepository {
    List<SalonChange> findPending(int page, int size);
    long countPending();
    Optional<SalonChange> findById(Long id);
    SalonChange save(SalonChange change);
}
