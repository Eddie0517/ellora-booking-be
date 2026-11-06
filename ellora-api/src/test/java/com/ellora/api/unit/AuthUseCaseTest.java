package com.ellora.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.common.port.MailSender;
import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.common.port.PendingRegistrationStore;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.LoginUserUseCase;
import com.ellora.application.feature.auth.RegisterUserUseCase;
import com.ellora.application.feature.auth.ResendVerificationUseCase;
import com.ellora.application.feature.auth.VerifyEmailUseCase;
import com.ellora.application.feature.auth.dto.LoginRequest;
import com.ellora.application.feature.auth.dto.RegisterRequest;
import com.ellora.application.feature.auth.dto.RegisterResponse;
import com.ellora.application.feature.auth.dto.ResendVerificationRequest;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.application.feature.auth.dto.VerifyEmailRequest;
import com.ellora.application.feature.auth.model.PendingRegistration;
import com.ellora.domain.constant.Role;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.RefreshToken;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import java.time.Duration;
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
class AuthUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PendingRegistrationStore pendingRegistrationStore;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private MailSender mailSender;

    private RegisterUserUseCase registerUserUseCase;
    private VerifyEmailUseCase verifyEmailUseCase;
    private ResendVerificationUseCase resendVerificationUseCase;
    private LoginUserUseCase loginUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(
                userRepository, pendingRegistrationStore, passwordHasher, tokenProvider, mailSender);
        verifyEmailUseCase = new VerifyEmailUseCase(
                userRepository, pendingRegistrationStore, refreshTokenRepository, tokenProvider);
        resendVerificationUseCase = new ResendVerificationUseCase(
                userRepository, pendingRegistrationStore, tokenProvider, mailSender);
        loginUserUseCase = new LoginUserUseCase(
                userRepository, refreshTokenRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("Register: Should store pending registration in Redis and NOT touch PostgreSQL database")
    void registerShouldSaveToRedisOnly() {
        String email = "customer@ellora.vn";
        RegisterRequest request = new RegisterRequest(email, "StrongPassword123", "Nguyen Van A", "0912345678");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordHasher.hash("StrongPassword123")).thenReturn("hashed-pw");
        when(tokenProvider.hashToken(anyString())).thenReturn("hashed-otp");

        RegisterResponse response = registerUserUseCase.execute(request);

        assertNotNull(response);
        assertEquals(email, response.email());
        assertNull(response.userId(), "User ID must be null prior to email verification");

        // Verify Redis was called with TTL
        ArgumentCaptor<PendingRegistration> pendingCaptor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationStore).save(pendingCaptor.capture(), eq(Duration.ofMinutes(15)));

        PendingRegistration saved = pendingCaptor.getValue();
        assertEquals(email, saved.email());
        assertEquals("hashed-pw", saved.passwordHash());
        assertEquals("Nguyen Van A", saved.fullName());
        assertEquals("hashed-otp", saved.otpHash());
        assertEquals(0, saved.attempts());

        // Verify DB was NOT modified
        verify(userRepository, never()).save(any(User.class));

        // Verify HTML email was sent
        verify(mailSender).sendHtml(eq(email), anyString(), eq("verify-email"), any());
    }

    @Test
    @DisplayName("VerifyEmail: Valid OTP should persist user in PostgreSQL, delete from Redis, and issue tokens")
    void verifyEmailShouldPersistUserAndIssueTokens() {
        String email = "customer@ellora.vn";
        String otp = "123456";
        String otpHash = "mock-otp-hash";

        PendingRegistration pending = new PendingRegistration(
                email, "hashed-pw", "Nguyen Van A", "0912345678", otpHash, 0, Instant.now());

        when(pendingRegistrationStore.findByEmail(email)).thenReturn(Optional.of(pending));
        when(tokenProvider.hashToken(otp)).thenReturn(otpHash);

        User savedUser = new User(
                UUID.randomUUID(), email, "hashed-pw", "Nguyen Van A", "0912345678", null,
                Role.CUSTOMER, Instant.now(), true, false, Instant.now(), Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(tokenProvider.generateAccessToken(any(), eq(email), eq("CUSTOMER"))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("refresh-token");
        when(tokenProvider.hashToken("refresh-token")).thenReturn("hashed-refresh-token");

        TokenResponse response = verifyEmailUseCase.execute(new VerifyEmailRequest(email, otp));

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertTrue(response.user().emailVerified());

        // DB persisted
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));

        // Redis cleaned up
        verify(pendingRegistrationStore).deleteByEmail(email);
    }

    @Test
    @DisplayName("VerifyEmail: Invalid OTP should increment attempts in Redis without touching DB")
    void verifyEmailInvalidOtpShouldIncrementAttempts() {
        String email = "customer@ellora.vn";
        PendingRegistration pending = new PendingRegistration(
                email, "hashed-pw", "Nguyen Van A", "0912345678", "hash-of-real-otp", 1, Instant.now());

        when(pendingRegistrationStore.findByEmail(email)).thenReturn(Optional.of(pending));
        when(tokenProvider.hashToken("999999")).thenReturn("hash-of-wrong-otp");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> verifyEmailUseCase.execute(new VerifyEmailRequest(email, "999999")));

        assertEquals("INVALID_OTP", ex.getErrorCode());

        // Verify attempts incremented in Redis
        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationStore).save(captor.capture(), eq(Duration.ofMinutes(15)));
        assertEquals(2, captor.getValue().attempts());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("ResendVerification: Should update Redis with new OTP and send email")
    void resendVerificationShouldOverwriteRedisWithNewOtp() {
        String email = "customer@ellora.vn";
        PendingRegistration pending = new PendingRegistration(
                email, "hashed-pw", "Nguyen Van A", "0912345678", "old-hash", 2, Instant.now());

        when(pendingRegistrationStore.findByEmail(email)).thenReturn(Optional.of(pending));
        when(tokenProvider.hashToken(anyString())).thenReturn("new-otp-hash");

        resendVerificationUseCase.execute(new ResendVerificationRequest(email));

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationStore).save(captor.capture(), eq(Duration.ofMinutes(15)));

        PendingRegistration updated = captor.getValue();
        assertEquals("new-otp-hash", updated.otpHash());
        assertEquals(0, updated.attempts(), "Attempts should be reset on resend");

        verify(mailSender).sendHtml(eq(email), anyString(), eq("verify-email"), any());
    }

    @Test
    @DisplayName("Login: Should reject unverified user even if password is correct")
    void loginShouldRejectUnverifiedUser() {
        String email = "unverified@ellora.vn";
        User unverifiedUser = new User(
                UUID.randomUUID(), email, "hashed-pw", "Unverified User", null, null,
                Role.CUSTOMER, null, true, false, Instant.now(), Instant.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(unverifiedUser));
        when(passwordHasher.matches("password", "hashed-pw")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> loginUserUseCase.execute(new LoginRequest(email, "password")));

        assertEquals("EMAIL_NOT_VERIFIED", ex.getErrorCode());
    }
}
