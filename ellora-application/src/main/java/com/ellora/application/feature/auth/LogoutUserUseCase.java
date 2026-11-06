package com.ellora.application.feature.auth;

import com.ellora.application.common.port.TokenBlacklist;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutUserUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklist tokenBlacklist;
    private final TokenProvider tokenProvider;

    @Transactional
    public void execute(UUID userId, String accessToken, String refreshTokenString) {
        // 1. Blacklist access token
        if (accessToken != null && !accessToken.isBlank()) {
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7).trim() : accessToken.trim();
            if (tokenProvider.validateToken(token)) {
                Instant exp = tokenProvider.getExpirationFromToken(token);
                String tokenHash = tokenProvider.hashToken(token);
                tokenBlacklist.revokeToken(tokenHash, exp);
            }
        }

        // 2. Revoke refresh token family
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            String rtHash = tokenProvider.hashToken(refreshTokenString.trim());
            Optional<RefreshToken> rtOpt = refreshTokenRepository.findByTokenHash(rtHash);
            if (rtOpt.isPresent()) {
                refreshTokenRepository.revokeAllByFamilyId(rtOpt.get().getFamilyId());
                return;
            }
        }

        // 3. Fallback: revoke all user refresh tokens
        if (userId != null) {
            refreshTokenRepository.revokeAllByUserId(userId);
        }
    }
}
