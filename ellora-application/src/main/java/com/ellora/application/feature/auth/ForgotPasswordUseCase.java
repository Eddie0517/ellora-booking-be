package com.ellora.application.feature.auth;

import com.ellora.application.common.port.MailSender;
import com.ellora.application.common.port.PasswordResetStore;
import com.ellora.application.feature.auth.dto.ForgotPasswordRequest;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetStore passwordResetStore;
    private final MailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public void execute(ForgotPasswordRequest request) {
        if (request == null || request.email() == null) {
            return;
        }

        String email = request.email().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return;
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        passwordResetStore.save(email, otp, Duration.ofMinutes(15));

        try {
            mailSender.sendText(
                    email,
                    "Mã đặt lại mật khẩu Ellora",
                    "Xin chào,\n\nMã xác nhận đặt lại mật khẩu của bạn là: " + otp +
                            "\n\nMã có hiệu lực trong vòng 15 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai."
            );
        } catch (Exception ignored) {
        }
    }
}
