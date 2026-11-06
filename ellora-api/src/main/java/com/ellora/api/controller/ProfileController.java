package com.ellora.api.controller;

import com.ellora.application.feature.auth.GetCurrentUserUseCase;
import com.ellora.application.feature.auth.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile compatibility endpoints")
public class ProfileController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile (legacy compatibility)", description = "Compatibility route for frontend GET /api/profiles/me")
    public ResponseEntity<UserProfileResponse> getCurrentProfile(@AuthenticationPrincipal UUID userId) {
        UserProfileResponse profile = getCurrentUserUseCase.execute(userId);
        return ResponseEntity.ok(profile);
    }
}
