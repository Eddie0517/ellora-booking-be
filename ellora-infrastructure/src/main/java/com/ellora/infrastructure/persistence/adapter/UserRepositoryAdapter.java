package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.User;
import com.ellora.domain.repository.UserRepository;
import com.ellora.infrastructure.persistence.entity.UserEntity;
import com.ellora.infrastructure.persistence.mapper.UserMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public List<User> findAll(int page, int pageSize) {
        return springDataUserRepository.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return springDataUserRepository.count();
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = springDataUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }
}
