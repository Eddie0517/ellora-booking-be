package com.ellora.infrastructure.adapter.security;

import com.ellora.application.common.port.GoogleTokenVerifier;
import com.ellora.application.feature.auth.dto.GoogleUserInfo;
import com.ellora.domain.exception.BusinessRuleException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifier {

    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierAdapter(@Value("${app.google.client-id:${GOOGLE_CLIENT_ID:}}") String clientId) {
        this.clientId = clientId != null ? clientId.trim() : "";
        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        );
        if (!this.clientId.isBlank()) {
            builder.setAudience(Collections.singletonList(this.clientId));
        }
        this.verifier = builder.build();
    }

    @Override
    public GoogleUserInfo verify(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new BusinessRuleException("INVALID_TOKEN", "Google credential token must not be blank");
        }
        try {
            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                log.warn("Google ID token verification failed (verifier returned null)");
                throw new BusinessRuleException("INVALID_TOKEN", "Xác thực token Google không hợp lệ hoặc đã hết hạn");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            return new GoogleUserInfo(
                    googleId,
                    email,
                    name != null ? name : "Google User",
                    pictureUrl,
                    Boolean.TRUE.equals(emailVerified)
            );
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Google ID token: {}", e.getMessage(), e);
            throw new BusinessRuleException("INVALID_TOKEN", "Không thể xác thực token Google: " + e.getMessage());
        }
    }
}
