package com.ellora.infrastructure.persistence.mapper;

import com.ellora.domain.constant.Role;
import com.ellora.domain.model.EmailVerificationToken;
import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.model.User;
import com.ellora.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.ellora.infrastructure.persistence.entity.RefreshTokenEntity;
import com.ellora.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        Role role = Role.CUSTOMER;
        if (entity.getRole() != null) {
            try {
                role = Role.valueOf(entity.getRole());
            } catch (IllegalArgumentException ignored) {}
        }

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getAvatarUrl(),
                role,
                entity.getEmailVerifiedAt(),
                entity.isEnabled(),
                entity.isLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        return UserEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .fullName(domain.getFullName())
                .phone(domain.getPhone())
                .avatarUrl(domain.getAvatarUrl())
                .role(domain.getRole() != null ? domain.getRole().name() : Role.CUSTOMER.name())
                .emailVerifiedAt(domain.getEmailVerifiedAt())
                .enabled(domain.isEnabled())
                .locked(domain.isLocked())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return new RefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getFamilyId(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getReplacedById(),
                entity.getCreatedAt()
        );
    }

    public RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }

        return RefreshTokenEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .tokenHash(domain.getTokenHash())
                .familyId(domain.getFamilyId())
                .expiresAt(domain.getExpiresAt())
                .revokedAt(domain.getRevokedAt())
                .replacedById(domain.getReplacedById())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return new EmailVerificationToken(
                entity.getId(),
                entity.getUserId(),
                entity.getCodeHash(),
                entity.getExpiresAt(),
                entity.getConsumedAt(),
                entity.getAttempts(),
                entity.getCreatedAt()
        );
    }

    public EmailVerificationTokenEntity toEntity(EmailVerificationToken domain) {
        if (domain == null) {
            return null;
        }

        return EmailVerificationTokenEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .codeHash(domain.getCodeHash())
                .expiresAt(domain.getExpiresAt())
                .consumedAt(domain.getConsumedAt())
                .attempts(domain.getAttempts())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
