package com.ellora.application.feature.auth;

import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.LoginRequest;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.application.feature.auth.dto.UserProfileResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Transactional
    public TokenResponse execute(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessRuleException("INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác"));

        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessRuleException("INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác");
        }

        if (!user.canAuthenticate()) {
            throw new BusinessRuleException("ACCOUNT_INACTIVE", "Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt");
        }

        UUID familyId = UUID.randomUUID();
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenString = tokenProvider.generateRefreshToken(user.getId(), familyId);
        String tokenHash = tokenProvider.hashToken(refreshTokenString);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                user.getId(),
                tokenHash,
                familyId,
                Instant.now().plus(7, ChronoUnit.DAYS),
                null,
                null,
                Instant.now()
        );
        refreshTokenRepository.save(refreshToken);

        UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getEmailVerifiedAt() != null
        );

        return TokenResponse.of(accessToken, refreshTokenString, 900, profile);
    }
}
