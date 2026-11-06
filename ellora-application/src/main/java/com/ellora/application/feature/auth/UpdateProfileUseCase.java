package com.ellora.application.feature.auth;

import com.ellora.application.feature.auth.dto.UpdateProfileRequest;
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
public class UpdateProfileUseCase {
    private final UserRepository users;

    @Transactional
    public UserProfileResponse execute(UUID userId, UpdateProfileRequest request) {
        if (userId == null) throw new BusinessRuleException("FORBIDDEN", "Vui lòng đăng nhập");
        User user = users.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Không tìm thấy tài khoản"));
        user.updateProfile(request.fullName(), request.phone(), null);
        User saved = users.save(user);
        return new UserProfileResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getPhone(),
                saved.getAvatarUrl(), saved.getRole().name(), saved.getEmailVerifiedAt() != null);
    }
}
