package com.vulncollab.auth;

import com.vulncollab.auth.dto.RegisterRequest;
import com.vulncollab.common.error.ConflictException;
import com.vulncollab.security.JwtService;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRepository;
import com.vulncollab.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService authService = new AuthService(userRepository, passwordEncoder, jwtService);

    @Test
    void registerCreatesUserRoleOnly() {
        RegisterRequest request = new RegisterRequest("New.User@Test.com", "New User", "Password123");
        Instant expiresAt = Instant.parse("2026-05-28T00:00:00Z");

        when(userRepository.existsByEmail("new.user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.accessTokenExpiresAt()).thenReturn(expiresAt);
        when(jwtService.createAccessToken(any(User.class), any(Instant.class))).thenReturn("access-token");

        var response = authService.register(request);

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.user().publicId())
                .matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        assertThat(response.user().email()).isEqualTo("new.user@test.com");
        assertThat(response.user().role()).isEqualTo(UserRole.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("alice@test.com", "Alice", "Password123");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email is already registered");
    }
}
