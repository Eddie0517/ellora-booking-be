package com.ellora.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.common.port.GoogleTokenVerifier;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.GoogleLoginUseCase;
import com.ellora.application.feature.auth.dto.GoogleLoginRequest;
import com.ellora.application.feature.auth.dto.GoogleUserInfo;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.domain.constant.Role;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleLoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    private GoogleLoginUseCase googleLoginUseCase;

    @BeforeEach
    void setUp() {
        googleLoginUseCase = new GoogleLoginUseCase(
                userRepository,
                refreshTokenRepository,
                tokenProvider,
                googleTokenVerifier
        );
    }

    @Test
    @DisplayName("Should login existing active user and issue tokens")
    void shouldLoginExistingActiveUser() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User(
                userId,
                "customer@example.com",
                null,
                "Existing Customer",
                "0987654321",
                "https://existing.png",
                Role.CUSTOMER,
                Instant.now(),
                true,
                false,
                Instant.now(),
                Instant.now()
        );

        when(googleTokenVerifier.verify("valid-credential")).thenReturn(
                new GoogleUserInfo("google-123", "customer@example.com", "Existing Customer", "https://new-avatar.png", true)
        );
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.generateAccessToken(eq(userId), eq("customer@example.com"), eq("CUSTOMER")))
                .thenReturn("access-token-xyz");
        when(tokenProvider.generateRefreshToken(eq(userId), any(UUID.class)))
                .thenReturn("refresh-token-xyz");
        when(tokenProvider.hashToken("refresh-token-xyz")).thenReturn("token-hash-xyz");

        TokenResponse response = googleLoginUseCase.execute(new GoogleLoginRequest("valid-credential"));

        assertNotNull(response);
        assertEquals("access-token-xyz", response.accessToken());
        assertEquals("refresh-token-xyz", response.refreshToken());
        assertEquals("customer@example.com", response.user().email());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should auto register new customer if user does not exist")
    void shouldAutoRegisterNewCustomer() {
        when(googleTokenVerifier.verify("new-credential")).thenReturn(
                new GoogleUserInfo("google-456", "newuser@example.com", "New Google User", "https://avatar.png", true)
        );
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.generateAccessToken(any(UUID.class), eq("newuser@example.com"), eq("CUSTOMER")))
                .thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), any(UUID.class)))
                .thenReturn("new-refresh-token");
        when(tokenProvider.hashToken("new-refresh-token")).thenReturn("hash-123");

        TokenResponse response = googleLoginUseCase.execute(new GoogleLoginRequest("new-credential"));

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertNotNull(savedUser.getEmailVerifiedAt());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when Google token is invalid")
    void shouldThrowWhenGoogleTokenInvalid() {
        when(googleTokenVerifier.verify("invalid-token")).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                googleLoginUseCase.execute(new GoogleLoginRequest("invalid-token")));

        assertEquals("INVALID_TOKEN", ex.getCode());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when email is not verified by Google")
    void shouldThrowWhenGoogleEmailNotVerified() {
        when(googleTokenVerifier.verify("unverified-token")).thenReturn(
                new GoogleUserInfo("google-789", "unverified@example.com", "Unverified", null, false)
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                googleLoginUseCase.execute(new GoogleLoginRequest("unverified-token")));

        assertEquals("EMAIL_NOT_VERIFIED", ex.getCode());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when existing user is locked or disabled")
    void shouldThrowWhenUserIsLocked() {
        User lockedUser = new User(
                UUID.randomUUID(),
                "locked@example.com",
                null,
                "Locked User",
                null,
                null,
                Role.CUSTOMER,
                Instant.now(),
                true,
                true, // locked
                Instant.now(),
                Instant.now()
        );

        when(googleTokenVerifier.verify("credential")).thenReturn(
                new GoogleUserInfo("google-999", "locked@example.com", "Locked User", null, true)
        );
        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(lockedUser));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                googleLoginUseCase.execute(new GoogleLoginRequest("credential")));

        assertEquals("ACCOUNT_INACTIVE", ex.getCode());
    }
}
