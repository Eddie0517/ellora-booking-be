package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.RevokedAccessTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRevokedAccessTokenRepository extends JpaRepository<RevokedAccessTokenEntity, String> {
}
