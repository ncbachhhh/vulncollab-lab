package com.vulncollab.auth;

import com.vulncollab.common.error.UnauthorizedException;
import com.vulncollab.security.JwtProperties;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final RefreshTokenService refreshTokenService = new RefreshTokenService(
            refreshTokenRepository,
            new JwtProperties("test-secret", 3_600_000, 604_800_000)
    );

    @Test
    void issueReturnsRawTokenButStoresOnlyHash() {
        User user = user();
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenIssue issued = refreshTokenService.issue(user, "127.0.0.1", "JUnit");

        assertThat(issued.token()).isNotBlank();
        assertThat(issued.expiresAt()).isAfter(Instant.now());
        var tokenCaptor = forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isNotEqualTo(issued.token());
    }

    @Test
    void consumeForRotationRevokesActiveToken() {
        RefreshToken storedToken = new RefreshToken(
                user(),
                "stored-hash",
                Instant.now().plusSeconds(300),
                "127.0.0.1",
                "JUnit"
        );
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        RefreshToken consumed = refreshTokenService.consumeForRotation("raw-refresh-token");

        assertThat(consumed.getRevokedAt()).isNotNull();
    }

    @Test
    void consumeForRotationRejectsExpiredToken() {
        RefreshToken storedToken = new RefreshToken(
                user(),
                "stored-hash",
                Instant.now().minusSeconds(1),
                "127.0.0.1",
                "JUnit"
        );
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.consumeForRotation("raw-refresh-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token is invalid or expired");
    }

    @Test
    void consumeForRotationRejectsReusedRevokedToken() {
        RefreshToken storedToken = new RefreshToken(
                user(),
                "stored-hash",
                Instant.now().plusSeconds(300),
                "127.0.0.1",
                "JUnit"
        );
        storedToken.revoke(Instant.now());
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.consumeForRotation("raw-refresh-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token is invalid or expired");
    }

    @Test
    void revokeRejectsMissingToken() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revoke("missing-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token is invalid or expired");
    }

    private User user() {
        return new User(
                "usr-test",
                "test@example.com",
                "hash",
                "Test User",
                UserRole.USER
        );
    }
}
