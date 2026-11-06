package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.infrastructure.persistence.entity.RefreshTokenEntity;
import com.ellora.infrastructure.persistence.mapper.UserMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return springDataRefreshTokenRepository.findByTokenHash(tokenHash).map(userMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = userMapper.toEntity(refreshToken);
        RefreshTokenEntity saved = springDataRefreshTokenRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllByFamilyId(UUID familyId) {
        springDataRefreshTokenRepository.revokeAllByFamilyId(familyId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllByUserId(UUID userId) {
        springDataRefreshTokenRepository.revokeAllByUserId(userId);
    }
}
