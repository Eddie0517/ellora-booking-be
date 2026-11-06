package com.ellora.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.api.security.JwtAuthenticationFilter;
import com.ellora.application.common.port.TokenProvider;
import com.ellora.application.feature.auth.GetCurrentUserUseCase;
import com.ellora.application.feature.auth.LoginUserUseCase;
import com.ellora.application.feature.auth.RefreshTokenUseCase;
import com.ellora.application.feature.auth.RegisterUserUseCase;
import com.ellora.application.feature.auth.dto.LoginRequest;
import com.ellora.application.feature.auth.dto.RegisterRequest;
import com.ellora.application.feature.auth.dto.TokenResponse;
import com.ellora.application.feature.auth.dto.UserProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created and TokenResponse")
    void shouldRegisterSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "test@example.com", "Test User", "0900000000", null, "CUSTOMER", false);
        TokenResponse tokenResponse = TokenResponse.of("mock-access-token", "mock-refresh-token", 900, profile);

        when(registerUserUseCase.execute(any(RegisterRequest.class))).thenReturn(tokenResponse);

        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User", "0900000000");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK and TokenResponse")
    void shouldLoginSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResponse profile = new UserProfileResponse(
                userId, "test@example.com", "Test User", "0900000000", null, "CUSTOMER", false);
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
}
