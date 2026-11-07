package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ellora.application.feature.auth.UpdateProfileUseCase;
import com.ellora.application.feature.auth.dto.UpdateProfileRequest;
import com.ellora.domain.constant.Role;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {
    @Mock UserRepository users;

    @Test
    void updatesOnlyTheCurrentUsersProfile() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("customer@example.com");
        user.setFullName("Old name");
        user.setRole(Role.CUSTOMER);
        when(users.findById(id)).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
        var result = new UpdateProfileUseCase(users).execute(id, new UpdateProfileRequest("New name", "0900000000", "https://img.com/avatar.jpg"));
        assertThat(result.fullName()).isEqualTo("New name");
        assertThat(result.phone()).isEqualTo("0900000000");
        assertThat(result.avatarUrl()).isEqualTo("https://img.com/avatar.jpg");
        assertThat(result.email()).isEqualTo("customer@example.com");
    }
}
