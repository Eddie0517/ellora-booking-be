package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.EmailVerificationToken;
import com.ellora.domain.repository.EmailVerificationTokenRepository;
import com.ellora.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.ellora.infrastructure.persistence.mapper.UserMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataEmailVerificationTokenRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {

    private final SpringDataEmailVerificationTokenRepository repository;
    private final UserMapper mapper;

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        EmailVerificationTokenEntity entity = mapper.toEntity(token);
        EmailVerificationTokenEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EmailVerificationToken> findLatestByUserId(UUID userId) {
        return repository.findFirstByUserIdOrderByCreatedAtDesc(userId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void invalidateAllActiveByUserId(UUID userId) {
        repository.invalidateAllActiveByUserId(userId);
    }
}
