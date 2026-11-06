package com.ellora.application.feature.auth;

import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.RegisterRequest;
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
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Transactional
    public TokenResponse execute(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("EMAIL_ALREADY_EXISTS", "Email này đã được đăng ký trong hệ thống");
        }

        String passwordHash = passwordHasher.hash(request.password());
        User newUser = User.createNewCustomer(normalizedEmail, passwordHash, request.fullName(), request.phone());
        User savedUser = userRepository.save(newUser);

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
                savedUser.getEmailVerifiedAt() != null
        );

        return TokenResponse.of(accessToken, refreshTokenString, 900, profile);
    }
}
