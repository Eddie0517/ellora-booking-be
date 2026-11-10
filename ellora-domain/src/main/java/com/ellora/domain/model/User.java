package com.ellora.domain.model;

import com.ellora.domain.constant.Role;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a User account.
 */
public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private Role role;
    private Instant emailVerifiedAt;
    private boolean enabled;
    private boolean locked;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(UUID id, String email, String passwordHash, String fullName, String phone,
                String avatarUrl, Role role, Instant emailVerifiedAt, boolean enabled, boolean locked,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : Role.CUSTOMER;
        this.emailVerifiedAt = emailVerifiedAt;
        this.enabled = enabled;
        this.locked = locked;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static User createNewCustomer(String email, String passwordHash, String fullName, String phone) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email.toLowerCase().trim(), passwordHash, fullName.trim(),
                phone != null ? phone.trim() : null, null, Role.CUSTOMER, null, true, false, now, now);
    }

    public static User createNewGoogleCustomer(String email, String fullName, String avatarUrl) {
        Instant now = Instant.now();
        String safeName = fullName != null && !fullName.isBlank() ? fullName.trim() : "Google User";
        return new User(UUID.randomUUID(), email.toLowerCase().trim(), null, safeName,
                null, avatarUrl != null && !avatarUrl.isBlank() ? avatarUrl.trim() : null,
                Role.CUSTOMER, now, true, false, now, now);
    }

    public boolean canAuthenticate() {
        return enabled && !locked;
    }

    public void updateProfile(String fullName, String phone, String avatarUrl) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName.trim();
        }
        if (phone != null) {
            this.phone = phone.trim();
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl.trim();
        }
        this.updatedAt = Instant.now();
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "Password hash must not be null");
        this.updatedAt = Instant.now();
    }

    public void markEmailVerified() {
        this.emailVerifiedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Instant emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
