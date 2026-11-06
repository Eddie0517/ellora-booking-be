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

    User save(User user);
}
