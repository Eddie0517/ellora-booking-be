package com.ellora.application.feature.admin;

import com.ellora.application.feature.admin.dto.AdminUserResponse;
import com.ellora.domain.constant.Role;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.RefreshTokenRepository;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LockUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SalonRepository salonRepository;

    @Transactional
    public AdminUserResponse execute(UUID userId, boolean locked) {
        if (userId == null) {
            throw new BusinessRuleException("INVALID_USER", "Mã người dùng không được để trống");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        user.setLocked(locked);
        User savedUser = userRepository.save(user);

        // Khi khóa/mở khóa tài khoản chủ salon, tự động đồng bộ trạng thái tạm ngưng/hoạt động của toàn bộ cơ sở salon
        if (user.getRole() == Role.SALON_OWNER) {
            List<Salon> salons = salonRepository.findByOwnerId(userId);
            for (Salon salon : salons) {
                if (locked) {
                    salon.suspend();
                } else {
                    salon.activate();
                }
                salonRepository.save(salon);
            }
        }

        // Với tài khoản thường (khách hàng/admin), thu hồi toàn bộ token đăng nhập khi bị khóa
        if (locked && user.getRole() != Role.SALON_OWNER) {
            try {
                refreshTokenRepository.revokeAllByUserId(userId);
            } catch (Exception ignored) {
            }
        }

        return new AdminUserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPhone(),
                savedUser.getRole() != null ? savedUser.getRole().name() : null,
                savedUser.isEnabled(),
                savedUser.isLocked(),
                savedUser.getEmailVerifiedAt(),
                savedUser.getCreatedAt()
        );
    }
}
