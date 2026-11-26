package com.ellora.domain.repository;

import com.ellora.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for User persistence.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<User> findAll(int page, int pageSize);

    long count();

    default java.util.List<User> search(String keyword, String role, int page, int pageSize) {
        return findAll(page, pageSize);
    }

    default long count(String keyword, String role) {
        return count();
    }

    User save(User user);
}
