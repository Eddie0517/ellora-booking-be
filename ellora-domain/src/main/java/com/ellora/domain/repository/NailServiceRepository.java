package com.ellora.domain.repository;

import com.ellora.domain.model.NailService;
import java.util.List;
import java.util.Optional;

public interface NailServiceRepository {

    Optional<NailService> findById(Long id);

    List<NailService> findBySalonId(Long salonId);

    List<NailService> findBySalonIdAndActiveTrue(Long salonId);

    NailService save(NailService service);

    void deleteById(Long id);
}
