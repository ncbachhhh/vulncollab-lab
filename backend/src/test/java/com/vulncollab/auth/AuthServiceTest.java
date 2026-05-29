package com.vulncollab.auth;

import com.vulncollab.auth.dto.LoginRequest;
import com.vulncollab.auth.dto.LogoutRequest;
import com.vulncollab.auth.dto.RefreshRequest;
import com.vulncollab.auth.dto.RegisterRequest;
import com.vulncollab.common.error.ConflictException;
import com.vulncollab.security.JwtService;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRepository;
import com.vulncollab.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final AuthService authService = new AuthService(
            userRepository,
            passwordEncoder,
            jwtService,
            refreshTokenService
    );

    @Test
    void registerCreatesUserRoleOnly() {
        RegisterRequest request = new RegisterRequest("New.User@Test.com", "New User", "Password123");
        Instant expiresAt = Instant.parse("2026-05-28T00:00:00Z");
        Instant refreshExpiresAt = Instant.parse("2026-06-04T00:00:00Z");

        when(userRepository.existsByEmail("new.user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.accessTokenExpiresAt()).thenReturn(expiresAt);
        when(jwtService.createAccessToken(any(User.class), any(Instant.class))).thenReturn("access-token");
        when(refreshTokenService.issue(any(User.class), eq("127.0.0.1"), eq("JUnit")))
                .thenReturn(new RefreshTokenIssue("refresh-token", refreshExpiresAt));

        var response = authService.register(request, "127.0.0.1", "JUnit");

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.refreshExpiresAt()).isEqualTo(refreshExpiresAt);
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

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1", "JUnit"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void loginIssuesAccessAndRefreshTokens() {
        User user = user();
        Instant accessExpiresAt = Instant.parse("2026-05-28T00:00:00Z");
        Instant refreshExpiresAt = Instant.parse("2026-06-04T00:00:00Z");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(jwtService.accessTokenExpiresAt()).thenReturn(accessExpiresAt);
        when(jwtService.createAccessToken(user, accessExpiresAt)).thenReturn("access-token");
        when(refreshTokenService.issue(user, "127.0.0.1", "JUnit"))
                .thenReturn(new RefreshTokenIssue("refresh-token", refreshExpiresAt));

        var response = authService.login(
                new LoginRequest("Alice@Test.com", "Password123"),
                "127.0.0.1",
                "JUnit"
        );

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.refreshExpiresAt()).isEqualTo(refreshExpiresAt);
    }

    @Test
    void refreshRotatesRefreshTokenAndReturnsNewTokens() {
        User user = user();
        RefreshToken consumedToken = new RefreshToken(
                user,
                "stored-hash",
                Instant.now().plusSeconds(300),
                "127.0.0.1",
                "JUnit"
        );
        Instant accessExpiresAt = Instant.parse("2026-05-28T00:00:00Z");
        Instant refreshExpiresAt = Instant.parse("2026-06-04T00:00:00Z");

        when(refreshTokenService.consumeForRotation("old-refresh-token")).thenReturn(consumedToken);
        when(jwtService.accessTokenExpiresAt()).thenReturn(accessExpiresAt);
        when(jwtService.createAccessToken(user, accessExpiresAt)).thenReturn("new-access-token");
        when(refreshTokenService.issue(user, "127.0.0.1", "JUnit"))
                .thenReturn(new RefreshTokenIssue("new-refresh-token", refreshExpiresAt));

        var response = authService.refresh(
                new RefreshRequest("old-refresh-token"),
                "127.0.0.1",
                "JUnit"
        );

        assertThat(response.token()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenService).consumeForRotation("old-refresh-token");
    }

    @Test
    void logoutRevokesRefreshToken() {
        authService.logout(new LogoutRequest("refresh-token"));

        verify(refreshTokenService).revoke("refresh-token");
    }

    private User user() {
        return new User(
                "usr-alice",
                "alice@test.com",
                "hash",
                "Alice",
                UserRole.USER
        );
    }
}
