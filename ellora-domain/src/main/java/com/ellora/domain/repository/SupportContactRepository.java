package com.ellora.domain.repository;

import com.ellora.domain.model.SupportContact;
import java.util.Optional;

public interface SupportContactRepository {
    Optional<SupportContact> get();
    SupportContact save(SupportContact contact);
}
