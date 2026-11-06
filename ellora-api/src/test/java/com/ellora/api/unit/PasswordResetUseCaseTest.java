package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ellora.application.common.port.MailSender;
import com.ellora.application.common.port.PasswordHasher;
import com.ellora.application.common.port.PasswordResetStore;
import com.ellora.application.feature.auth.ConfirmForgotPasswordUseCase;
import com.ellora.application.feature.auth.ForgotPasswordUseCase;
import com.ellora.application.feature.auth.dto.ConfirmForgotPasswordRequest;
import com.ellora.application.feature.auth.dto.ForgotPasswordRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetStore passwordResetStore;
    @Mock
    private MailSender mailSender;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private ForgotPasswordUseCase forgotPasswordUseCase;
    private ConfirmForgotPasswordUseCase confirmForgotPasswordUseCase;

    @BeforeEach
    void setUp() {
        forgotPasswordUseCase = new ForgotPasswordUseCase(userRepository, passwordResetStore, mailSender);
        confirmForgotPasswordUseCase = new ConfirmForgotPasswordUseCase(userRepository, passwordResetStore, passwordHasher, refreshTokenRepository);
    }

    @Test
    @DisplayName("ForgotPassword: Sends OTP email when user exists")
    void forgotPassword_existingUser_sendsOtp() {
        User user = new User();
        user.setEmail("test@ellora.vn");
        when(userRepository.findByEmail("test@ellora.vn")).thenReturn(Optional.of(user));

        forgotPasswordUseCase.execute(new ForgotPasswordRequest("test@ellora.vn"));

        verify(passwordResetStore).save(eq("test@ellora.vn"), any(), any());
        verify(mailSender).sendText(eq("test@ellora.vn"), contains("đặt lại mật khẩu"), any());
    }

    @Test
    @DisplayName("ConfirmForgotPassword: Success updates password and clears OTP")
    void confirmForgotPassword_success() {
        String email = "test@ellora.vn";
        String otp = "123456";
        String newPassword = "newPassword123";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);

        when(passwordResetStore.findOtpByEmail(email)).thenReturn(Optional.of(otp));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.hash(newPassword)).thenReturn("hashed-new-password");

        confirmForgotPasswordUseCase.execute(new ConfirmForgotPasswordRequest(email, otp, newPassword));

        verify(userRepository).save(user);
        verify(passwordResetStore).deleteByEmail(email);
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    @DisplayName("ConfirmForgotPassword: Fails when OTP does not match")
    void confirmForgotPassword_invalidOtp_throwsException() {
        String email = "test@ellora.vn";
        when(passwordResetStore.findOtpByEmail(email)).thenReturn(Optional.of("123456"));

        assertThatThrownBy(() -> confirmForgotPasswordUseCase.execute(new ConfirmForgotPasswordRequest(email, "999999", "newPassword123")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Mã xác nhận không đúng hoặc đã hết hạn");
    }
}
