package com.ellora.application.common.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for inspecting the currently authenticated user security context.
 */
public interface CurrentUser {

    Optional<UUID> getUserId();

    Optional<String> getEmail();

    Optional<String> getRole();

    boolean isAuthenticated();
}
