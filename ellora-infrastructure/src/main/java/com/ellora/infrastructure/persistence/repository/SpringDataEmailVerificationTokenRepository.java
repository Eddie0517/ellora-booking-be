package com.ellora.infrastructure.persistence.repository;

import com.ellora.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {

    Optional<EmailVerificationTokenEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE EmailVerificationTokenEntity e SET e.consumedAt = CURRENT_TIMESTAMP WHERE e.userId = :userId AND e.consumedAt IS NULL")
    void invalidateAllActiveByUserId(@Param("userId") UUID userId);
}
