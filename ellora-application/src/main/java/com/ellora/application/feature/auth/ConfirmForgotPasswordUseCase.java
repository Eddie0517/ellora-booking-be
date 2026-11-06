package com.ellora.application.feature.auth;

import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.common.port.PasswordResetStore;
import com.ellora.application.feature.auth.dto.ConfirmForgotPasswordRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetStore passwordResetStore;
    private final PasswordHasher passwordHasher;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(ConfirmForgotPasswordRequest request) {
        if (request == null || request.email() == null || request.confirmationCode() == null) {
            throw new BusinessRuleException("INVALID_REQUEST", "Thông tin yêu cầu không hợp lệ");
        }

        String email = request.email().toLowerCase().trim();
        String savedOtp = passwordResetStore.findOtpByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("INVALID_OTP", "Mã xác nhận không đúng hoặc đã hết hạn."));

        if (!savedOtp.equals(request.confirmationCode().trim())) {
            throw new BusinessRuleException("INVALID_OTP", "Mã xác nhận không đúng hoặc đã hết hạn.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Tài khoản không tồn tại."));

        user.changePassword(passwordHasher.hash(request.newPassword()));
        userRepository.save(user);

        // Clear OTP from redis
        passwordResetStore.deleteByEmail(email);

        // Revoke active refresh tokens
        try {
            refreshTokenRepository.revokeAllByUserId(user.getId());
        } catch (Exception ignored) {
        }
    }
}
