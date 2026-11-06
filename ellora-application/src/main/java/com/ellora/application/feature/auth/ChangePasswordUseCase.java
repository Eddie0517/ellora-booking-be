package com.ellora.application.feature.auth;

import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.feature.auth.dto.ChangePasswordRequest;
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
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public void execute(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Không tìm thấy thông tin tài khoản"));

        if (!passwordHasher.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("INVALID_PASSWORD", "Mật khẩu hiện tại không chính xác");
        }

        String newHash = passwordHasher.hash(request.newPassword());
        user.changePassword(newHash);
        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
