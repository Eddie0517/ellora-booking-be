package com.ellora.application.feature.auth;

import com.ellora.application.common.port.PendingRegistrationStore;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.application.feature.auth.dto.UserProfileResponse;
import com.ellora.application.feature.auth.dto.VerifyEmailRequest;
import com.ellora.application.feature.auth.model.PendingRegistration;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration PENDING_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PendingRegistrationStore pendingRegistrationStore;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public TokenResponse execute(VerifyEmailRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        Optional<PendingRegistration> pendingOpt = pendingRegistrationStore.findByEmail(normalizedEmail);
        if (pendingOpt.isEmpty()) {
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new BusinessRuleException("ALREADY_VERIFIED", "Tài khoản đã được xác thực trước đó. Vui lòng đăng nhập.");
            }
            throw new BusinessRuleException("INVALID_OTP", "Mã xác thực đã hết hạn hoặc không tồn tại. Vui lòng đăng ký lại.");
        }

        PendingRegistration pending = pendingOpt.get();

        if (pending.attempts() >= MAX_ATTEMPTS) {
            pendingRegistrationStore.deleteByEmail(normalizedEmail);
            throw new BusinessRuleException("OTP_MAX_ATTEMPTS_EXCEEDED", "Bạn đã nhập sai mã xác thực quá số lần cho phép (5 lần). Vui lòng đăng ký lại.");
        }

        String inputHash = tokenProvider.hashToken(request.code().trim());
        boolean isMatched = MessageDigest.isEqual(
                pending.otpHash().getBytes(StandardCharsets.UTF_8),
                inputHash.getBytes(StandardCharsets.UTF_8)
        );

        if (!isMatched) {
            pendingRegistrationStore.save(pending.withIncrementedAttempts(), PENDING_TTL);
            int remaining = MAX_ATTEMPTS - (pending.attempts() + 1);
            throw new BusinessRuleException("INVALID_OTP", "Mã xác thực OTP không chính xác. Bạn còn " + remaining + " lần thử.");
        }

        // OTP is valid! Persist User to PostgreSQL database now
        User newUser = User.createNewCustomer(
                pending.email(),
                pending.passwordHash(),
                pending.fullName(),
                pending.phone()
        );
        newUser.markEmailVerified();
        User savedUser = userRepository.save(newUser);

        // Remove unverified registration from Redis
        pendingRegistrationStore.deleteByEmail(normalizedEmail);

        // Issue tokens upon successful email verification
        UUID familyId = UUID.randomUUID();
        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        String refreshTokenString = tokenProvider.generateRefreshToken(savedUser.getId(), familyId);
        String tokenHash = tokenProvider.hashToken(refreshTokenString);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                savedUser.getId(),
                tokenHash,
                familyId,
                Instant.now().plus(7, ChronoUnit.DAYS),
                null,
                null,
                Instant.now()
        );
        refreshTokenRepository.save(refreshToken);

        UserProfileResponse profile = new UserProfileResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPhone(),
                savedUser.getAvatarUrl(),
                savedUser.getRole().name(),
                true
        );

        return TokenResponse.of(accessToken, refreshTokenString, 900, profile);
    }
}
