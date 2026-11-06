package com.ellora.application.feature.auth;

import com.ellora.application.feature.auth.dto.UserProfileResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Không tìm thấy thông tin tài khoản"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getEmailVerifiedAt() != null
        );
    }
}
