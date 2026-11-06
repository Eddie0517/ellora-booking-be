package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.User;
import com.ellora.domain.repository.UserRepository;
import com.ellora.infrastructure.persistence.entity.UserEntity;
import com.ellora.infrastructure.persistence.mapper.UserMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataUserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = springDataUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }
}
