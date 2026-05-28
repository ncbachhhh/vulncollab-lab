package com.vulncollab.security;

import com.vulncollab.user.User;
import com.vulncollab.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(
            new JwtProperties("test-secret", 3_600_000, 604_800_000)
    );

    @Test
    void validatesSignedTokenAndExtractsClaims() {
        User user = new User(
                "usr-test",
                "test@example.com",
                "hash",
                "Test User",
                UserRole.USER
        );

        String token = jwtService.createAccessToken(user, Instant.now().plusSeconds(300));

        var claims = jwtService.validateAccessToken(token);

        assertThat(claims).isPresent();
        assertThat(claims.get().subject()).isEqualTo("usr-test");
        assertThat(claims.get().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void rejectsTamperedToken() {
        User user = new User(
                "usr-test",
                "test@example.com",
                "hash",
                "Test User",
                UserRole.USER
        );

        String token = jwtService.createAccessToken(user, Instant.now().plusSeconds(300));
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.validateAccessToken(tamperedToken)).isEmpty();
    }
}
