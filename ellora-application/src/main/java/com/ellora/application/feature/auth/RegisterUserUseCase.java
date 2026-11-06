package com.ellora.application.feature.auth;

import com.ellora.application.common.port.MailSender;
import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.common.port.PendingRegistrationStore;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.RegisterRequest;
import com.ellora.application.feature.auth.dto.RegisterResponse;
import com.ellora.application.feature.auth.model.PendingRegistration;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private static final Duration PENDING_REGISTRATION_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PendingRegistrationStore pendingRegistrationStore;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final MailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterResponse execute(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("EMAIL_ALREADY_EXISTS", "Email này đã được đăng ký trong hệ thống");
        }

        String passwordHash = passwordHasher.hash(request.password());

        // Generate 6-digit OTP code
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String otpHash = tokenProvider.hashToken(otp);

        // Store pending registration in Redis (supports overwrite on re-register)
        PendingRegistration pendingRegistration = new PendingRegistration(
                normalizedEmail,
                passwordHash,
                request.fullName() != null ? request.fullName().trim() : null,
                request.phone() != null ? request.phone().trim() : null,
                otpHash,
                0,
                Instant.now()
        );
        pendingRegistrationStore.save(pendingRegistration, PENDING_REGISTRATION_TTL);

        // Send OTP verification email
        mailSender.sendHtml(
                normalizedEmail,
                "Mã xác thực tài khoản Ellora Booking",
                "verify-email",
                Map.of(
                        "message", "Cảm ơn bạn đã đăng ký tài khoản tại Ellora Booking. Mã xác thực (OTP) của bạn là:",
                        "code", otp
                )
        );

        return RegisterResponse.of(normalizedEmail);
    }
}
