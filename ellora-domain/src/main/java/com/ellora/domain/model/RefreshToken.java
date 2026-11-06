package com.ellora.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a rotating Refresh Token.
 */
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private UUID familyId;
    private Instant expiresAt;
    private Instant revokedAt;
    private UUID replacedById;
    private Instant createdAt;

    public RefreshToken() {
    }

    public RefreshToken(UUID id, UUID userId, String tokenHash, UUID familyId,
                        Instant expiresAt, Instant revokedAt, UUID replacedById, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId != null ? familyId : UUID.randomUUID();
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.replacedById = replacedById;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public boolean isValid() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }

    public void revoke(UUID replacedById) {
        this.revokedAt = Instant.now();
        this.replacedById = replacedById;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public UUID getFamilyId() { return familyId; }
    public void setFamilyId(UUID familyId) { this.familyId = familyId; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public UUID getReplacedById() { return replacedById; }
    public void setReplacedById(UUID replacedById) { this.replacedById = replacedById; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
