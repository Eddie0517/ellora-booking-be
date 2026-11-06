package com.ellora.application.feature.auth;

import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.dto.RefreshTokenRequest;
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
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public TokenResponse execute(RefreshTokenRequest request) {
        String tokenString = request.refreshToken();
        if (!tokenProvider.validateToken(tokenString)) {
            throw new BusinessRuleException("INVALID_REFRESH_TOKEN", "Mã refresh token không hợp lệ hoặc đã hết hạn");
        }

        String tokenHash = tokenProvider.hashToken(tokenString);
        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("INVALID_REFRESH_TOKEN", "Mã refresh token không tồn tại"));

        if (!existingToken.isValid()) {
            refreshTokenRepository.revokeAllByFamilyId(existingToken.getFamilyId());
            throw new BusinessRuleException("TOKEN_REUSE_DETECTED", "Phát hiện tái sử dụng refresh token đã hủy. Toàn bộ phiên đăng nhập đã bị vô hiệu hóa.");
        }

        User user = userRepository.findById(existingToken.getUserId())
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Không tìm thấy thông tin tài khoản"));

        if (!user.canAuthenticate()) {
            throw new BusinessRuleException("ACCOUNT_INACTIVE", "Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt");
        }

        UUID newId = UUID.randomUUID();
        String newRefreshTokenString = tokenProvider.generateRefreshToken(user.getId(), existingToken.getFamilyId());
        String newTokenHash = tokenProvider.hashToken(newRefreshTokenString);

        existingToken.revoke(newId);
        refreshTokenRepository.save(existingToken);

        RefreshToken newToken = new RefreshToken(
                newId,
                user.getId(),
                newTokenHash,
                existingToken.getFamilyId(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                null,
                null,
                Instant.now()
        );
        refreshTokenRepository.save(newToken);

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getEmailVerifiedAt() != null
        );

        return TokenResponse.of(newAccessToken, newRefreshTokenString, 900, profile);
    }
}
