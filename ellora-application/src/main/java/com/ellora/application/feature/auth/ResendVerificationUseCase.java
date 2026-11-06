package com.ellora.application.feature.auth;

import com.ellora.application.common.port.MailSender;
import com.ellora.application.common.port.PendingRegistrationStore;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.ResendVerificationRequest;
import com.ellora.application.feature.auth.model.PendingRegistration;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendVerificationUseCase {

    private static final Duration PENDING_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PendingRegistrationStore pendingRegistrationStore;
    private final TokenProvider tokenProvider;
    private final MailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public void execute(ResendVerificationRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        Optional<PendingRegistration> pendingOpt = pendingRegistrationStore.findByEmail(normalizedEmail);
        if (pendingOpt.isEmpty()) {
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new BusinessRuleException("ALREADY_VERIFIED", "Tài khoản đã được xác thực trước đó. Vui lòng đăng nhập.");
            }
            throw new BusinessRuleException("REGISTRATION_NOT_FOUND", "Không tìm thấy yêu cầu đăng ký chưa hoàn tất. Vui lòng thực hiện đăng ký tài khoản.");
        }

        PendingRegistration pending = pendingOpt.get();

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String newOtpHash = tokenProvider.hashToken(otp);

        PendingRegistration updated = pending.withNewOtp(newOtpHash);
        pendingRegistrationStore.save(updated, PENDING_TTL);

        mailSender.sendHtml(
                normalizedEmail,
                "Mã xác thực tài khoản mới - Ellora Booking",
                "verify-email",
                Map.of(
                        "message", "Mã xác thực tài khoản mới (OTP) của bạn là:",
                        "code", otp
                )
        );
    }
}
