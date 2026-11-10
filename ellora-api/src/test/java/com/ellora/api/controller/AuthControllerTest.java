package com.ellora.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.feature.auth.GetCurrentUserUseCase;
import com.ellora.application.feature.auth.GoogleLoginUseCase;
import com.ellora.application.feature.auth.LoginUserUseCase;
import com.ellora.application.feature.auth.RefreshTokenUseCase;
import com.ellora.application.feature.auth.RegisterUserUseCase;
import com.ellora.application.feature.auth.ResendVerificationUseCase;
import com.ellora.application.feature.auth.VerifyEmailUseCase;
import com.ellora.application.feature.auth.dto.GoogleLoginRequest;
import com.ellora.application.feature.auth.dto.LoginRequest;
import com.ellora.application.feature.auth.dto.RegisterRequest;
import com.ellora.application.feature.auth.dto.RegisterResponse;
import com.ellora.application.feature.auth.dto.ResendVerificationRequest;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.application.feature.auth.dto.UserProfileResponse;
import com.ellora.application.feature.auth.dto.VerifyEmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @MockBean
    private ResendVerificationUseCase resendVerificationUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private GoogleLoginUseCase googleLoginUseCase;

    @MockBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created and RegisterResponse")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterResponse registerResponse = RegisterResponse.of("test@example.com");

        when(registerUserUseCase.execute(any(RegisterRequest.class))).thenReturn(registerResponse);

        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User", "0900000000");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify-email should return 200 OK and TokenResponse")
    void shouldVerifyEmailSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "test@example.com", "Test User", "0900000000", null, "CUSTOMER", true);
        TokenResponse tokenResponse = TokenResponse.of("mock-access-token", "mock-refresh-token", 900, profile);

        when(verifyEmailUseCase.execute(any(VerifyEmailRequest.class))).thenReturn(tokenResponse);

        VerifyEmailRequest request = new VerifyEmailRequest("test@example.com", "123456");

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/resend-verification should return 200 OK")
    void shouldResendVerificationSuccessfully() throws Exception {
        doNothing().when(resendVerificationUseCase).execute(any(ResendVerificationRequest.class));

        ResendVerificationRequest request = new ResendVerificationRequest("test@example.com");

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK and TokenResponse")
    void shouldLoginSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "test@example.com", "Test User", "0900000000", null, "CUSTOMER", true);
        TokenResponse tokenResponse = TokenResponse.of("mock-access-token", "mock-refresh-token", 900, profile);

        when(loginUserUseCase.execute(any(LoginRequest.class))).thenReturn(tokenResponse);

        LoginRequest request = new LoginRequest("test@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/google should return 200 OK and TokenResponse")
    void shouldLoginWithGoogleSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "googleuser@example.com", "Google User", null, "https://avatar.png", "CUSTOMER", true);
        TokenResponse tokenResponse = TokenResponse.of("mock-google-access", "mock-google-refresh", 900, profile);

        when(googleLoginUseCase.execute(any(GoogleLoginRequest.class))).thenReturn(tokenResponse);

        GoogleLoginRequest request = new GoogleLoginRequest("google-credential-token");

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-google-access"))
                .andExpect(jsonPath("$.user.email").value("googleuser@example.com"));
    }
}
