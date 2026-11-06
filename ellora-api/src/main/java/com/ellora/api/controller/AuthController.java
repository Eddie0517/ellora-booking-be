package com.ellora.api.controller;

import com.ellora.application.feature.auth.ChangePasswordUseCase;
import com.ellora.application.feature.auth.ConfirmForgotPasswordUseCase;
import com.ellora.application.feature.auth.ForgotPasswordUseCase;
import com.ellora.application.feature.auth.GetCurrentUserUseCase;
import com.ellora.application.feature.auth.LoginUserUseCase;
import com.ellora.application.feature.auth.LogoutUserUseCase;
import com.ellora.application.feature.auth.RefreshTokenUseCase;
import com.ellora.application.feature.auth.RegisterUserUseCase;
import com.ellora.application.feature.auth.ResendVerificationUseCase;
import com.ellora.application.feature.auth.VerifyEmailUseCase;
import com.ellora.application.feature.auth.UpdateProfileUseCase;
import com.ellora.application.feature.auth.dto.UpdateProfileRequest;
import com.ellora.application.feature.auth.dto.ChangePasswordRequest;
import com.ellora.application.feature.auth.dto.ConfirmForgotPasswordRequest;
import com.ellora.application.feature.auth.dto.ForgotPasswordRequest;
import com.ellora.application.feature.auth.dto.LoginRequest;
import com.ellora.application.feature.auth.dto.RefreshTokenRequest;
import com.ellora.application.feature.auth.dto.RegisterRequest;
import com.ellora.application.feature.auth.dto.RegisterResponse;
import com.ellora.application.feature.auth.dto.ResendVerificationRequest;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.application.feature.auth.dto.UserProfileResponse;
import com.ellora.application.feature.auth.dto.VerifyEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, OTP email verification, authentication and token refresh")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ConfirmForgotPasswordUseCase confirmForgotPasswordUseCase;

    @PostMapping("/register")
    @Operation(summary = "Customer registration", description = "Registers a new customer account and sends 6-digit OTP verification email")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP", description = "Verifies customer email with 6-digit OTP code and returns initial JWT credentials")
    public ResponseEntity<TokenResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        TokenResponse response = verifyEmailUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification OTP", description = "Generates and sends a new 6-digit OTP code to the user's email")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        resendVerificationUseCase.execute(request);
        return ResponseEntity.ok(Map.of("message", "Mã xác thực mới đã được gửi tới email của bạn."));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates credentials and returns access & refresh tokens (requires verified email)")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = loginUserUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token rotation", description = "Rotates refresh token and issues a new access token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile", description = "Returns profile details of the authenticated user")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UUID userId) {
        UserProfileResponse profile = getCurrentUserUseCase.execute(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(updateProfileUseCase.execute(userId, request));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User logout", description = "Revokes refresh tokens and blacklists the current access token")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UUID userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.refreshToken() : null;
        logoutUserUseCase.execute(userId, authHeader, refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password", description = "Changes password for authenticated user and revokes active sessions")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.execute(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password request", description = "Sends 6-digit OTP code to user email if account exists")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request);
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại trong hệ thống, mã xác nhận đã được gửi đến hộp thư của bạn."));
    }

    @PostMapping("/confirm-forgot-password")
    @Operation(summary = "Confirm forgot password", description = "Verifies 6-digit OTP code and updates account password")
    public ResponseEntity<Map<String, String>> confirmForgotPassword(@Valid @RequestBody ConfirmForgotPasswordRequest request) {
        confirmForgotPasswordUseCase.execute(request);
        return ResponseEntity.ok(Map.of("message", "Mật khẩu của bạn đã được cập nhật thành công. Vui lòng đăng nhập lại."));
    }
}
