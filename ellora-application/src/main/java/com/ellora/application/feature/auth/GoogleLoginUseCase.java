package com.ellora.application.feature.auth;

import com.ellora.application.common.port.GoogleTokenVerifier;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.GoogleLoginRequest;
import com.ellora.application.feature.auth.dto.GoogleUserInfo;
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
public class GoogleLoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public TokenResponse execute(GoogleLoginRequest request) {
        GoogleUserInfo userInfo = googleTokenVerifier.verify(request.credential());
        if (userInfo == null || userInfo.email() == null || userInfo.email().isBlank()) {
            throw new BusinessRuleException("INVALID_TOKEN", "Google credential is invalid or could not be verified");
        }

        if (!userInfo.emailVerified()) {
            throw new BusinessRuleException("EMAIL_NOT_VERIFIED", "Tài khoản Google chưa được xác thực email");
        }

        String normalizedEmail = userInfo.email().toLowerCase().trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .map(existingUser -> {
                    if (!existingUser.canAuthenticate()) {
                        throw new BusinessRuleException("ACCOUNT_INACTIVE", "Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt");
                    }
                    if (existingUser.getEmailVerifiedAt() == null) {
                        existingUser.markEmailVerified();
                    }
                    if ((existingUser.getAvatarUrl() == null || existingUser.getAvatarUrl().isBlank())
                            && userInfo.avatarUrl() != null && !userInfo.avatarUrl().isBlank()) {
                        existingUser.setAvatarUrl(userInfo.avatarUrl());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.createNewGoogleCustomer(
                            normalizedEmail,
                            userInfo.fullName(),
                            userInfo.avatarUrl()
                    );
                    return userRepository.save(newUser);
                });

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
