package com.ellora.application.feature.admin;

import com.ellora.application.feature.admin.dto.AdminUserResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LockUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AdminUserResponse execute(UUID userId, boolean locked) {
        if (userId == null) {
            throw new BusinessRuleException("INVALID_USER", "Mã người dùng không được để trống");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        user.setLocked(locked);
        User savedUser = userRepository.save(user);

        if (locked) {
            try {
                refreshTokenRepository.revokeAllByUserId(userId);
            } catch (Exception ignored) {
            }
        }

        return new AdminUserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPhone(),
                savedUser.getRole() != null ? savedUser.getRole().name() : null,
                savedUser.isEnabled(),
                savedUser.isLocked(),
                savedUser.getEmailVerifiedAt(),
                savedUser.getCreatedAt()
        );
    }
}
