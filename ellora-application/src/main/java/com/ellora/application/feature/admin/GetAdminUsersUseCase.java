package com.ellora.application.feature.admin;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.admin.dto.AdminUserResponse;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminUsersUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResult<AdminUserResponse> execute(int page, int pageSize) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = (pageSize <= 0 || pageSize > 100) ? 20 : pageSize;

        List<User> users = userRepository.findAll(normalizedPage, normalizedSize);
        long totalItems = userRepository.count();

        List<AdminUserResponse> items = users.stream().map(u -> new AdminUserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getPhone(),
                u.getRole() != null ? u.getRole().name() : null,
                u.isEnabled(),
                u.isLocked(),
                u.getEmailVerifiedAt(),
                u.getCreatedAt()
        )).toList();

        return PagedResult.of(items, normalizedPage, normalizedSize, totalItems);
    }
}
