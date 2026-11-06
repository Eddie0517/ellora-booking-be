package com.ellora.infrastructure.adapter.security;

import com.ellora.application.common.port.TokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Hs256TokenProvider implements TokenProvider {

    private final String secret;
    private final String issuer;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;
    private final ObjectMapper objectMapper;

    public Hs256TokenProvider(
            @Value("${app.jwt.secret:${JWT_SECRET:ellora_default_development_secret_key_must_be_at_least_256_bits_long}}") String secret,
            @Value("${app.jwt.issuer:${JWT_ISSUER:ellora-booking-be}}") String issuer,
            @Value("${app.jwt.access-expiration-seconds:${JWT_ACCESS_EXPIRATION_SECONDS:900}}") long accessExpirationSeconds,
            @Value("${app.jwt.refresh-expiration-seconds:${JWT_REFRESH_EXPIRATION_SECONDS:604800}}") long refreshExpirationSeconds,
            ObjectMapper objectMapper) {
        this.secret = secret;
        this.issuer = issuer;
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateAccessToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpirationSeconds);

        ObjectNode payload = objectMapper.createObjectNode()
                .put("jti", UUID.randomUUID().toString())
                .put("sub", userId.toString())
                .put("email", email)
                .put("role", role)
                .put("type", "ACCESS")
                .put("iss", issuer)
                .put("iat", now.getEpochSecond())
                .put("exp", exp.getEpochSecond());

        return signToken(payload);
    }

    @Override
    public String generateRefreshToken(UUID userId, UUID familyId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshExpirationSeconds);

        ObjectNode payload = objectMapper.createObjectNode()
                .put("jti", UUID.randomUUID().toString())
                .put("sub", userId.toString())
                .put("familyId", familyId != null ? familyId.toString() : UUID.randomUUID().toString())
                .put("type", "REFRESH")
                .put("iss", issuer)
                .put("iat", now.getEpochSecond())
                .put("exp", exp.getEpochSecond());

        return signToken(payload);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return false;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = signHmac(content);
            if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            JsonNode payload = parsePayload(parts[1]);
            long exp = payload.path("exp").asLong(0);
            return Instant.now().getEpochSecond() < exp;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        try {
            JsonNode payload = getPayloadNode(token);
            return UUID.fromString(payload.path("sub").asText());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token subject: " + e.getMessage(), e);
        }
    }

    @Override
    public String getRoleFromToken(String token) {
        try {
            JsonNode payload = getPayloadNode(token);
            return payload.path("role").asText("CUSTOMER");
        } catch (Exception e) {
            return "CUSTOMER";
        }
    }

    @Override
    public Instant getExpirationFromToken(String token) {
        try {
            JsonNode payload = getPayloadNode(token);
            long exp = payload.path("exp").asLong(0);
            return Instant.ofEpochSecond(exp);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    @Override
    public String hashToken(String token) {
        if (token == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate SHA-256 hash", e);
        }
    }

    private String signToken(ObjectNode payload) {
        try {
            ObjectNode header = objectMapper.createObjectNode()
                    .put("alg", "HS256")
                    .put("typ", "JWT");

            String headerBase64 = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String payloadBase64 = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String content = headerBase64 + "." + payloadBase64;
            String signature = signHmac(content);

            return content + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT token", e);
        }
    }

    private String signHmac(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(hmacBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Error computing HMAC-SHA256", e);
        }
    }

    private JsonNode getPayloadNode(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed JWT token");
        }
        return parsePayload(parts[1]);
    }

    private JsonNode parsePayload(String payloadBase64) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(payloadBase64);
        return objectMapper.readTree(bytes);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
