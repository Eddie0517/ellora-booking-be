package com.ellora.domain.repository;

import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.model.Salon;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalonRepository {

    Optional<Salon> findById(Long id);

    List<Salon> findByStatus(SalonStatus status, int offset, int limit);

    List<Salon> searchActiveSalons(String city, String keyword, int offset, int limit);

    long countActiveSalons(String city, String keyword);

    List<Salon> findByOwnerId(UUID ownerId);

    long countByStatus(SalonStatus status);

    long count();

    List<Salon> findAll();

    List<Salon> findAll(int offset, int limit);

    Salon save(Salon salon);
}
