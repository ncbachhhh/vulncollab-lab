package com.vulncollab.auth;

import com.vulncollab.common.error.UnauthorizedException;
import com.vulncollab.security.JwtProperties;
import com.vulncollab.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private static final int RANDOM_TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public RefreshTokenIssue issue(User user, String ipAddress, String userAgent) {
        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs());
        RefreshToken saved = refreshTokenRepository.save(new RefreshToken(
                user,
                hash(rawToken),
                expiresAt,
                limitLength(ipAddress, 64),
                limitLength(userAgent, 512)
        ));

        return new RefreshTokenIssue(rawToken, saved.getExpiresAt());
    }

    @Transactional
    public RefreshToken consumeForRotation(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> invalidRefreshToken());

        // Rotation makes a refresh token one-time use, so replay attempts are rejected.
        Instant now = Instant.now();
        if (!token.isActive(now)) {
            throw invalidRefreshToken();
        }

        token.revoke(now);
        return token;
    }

    @Transactional
    public void revoke(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> invalidRefreshToken());

        Instant now = Instant.now();
        if (!token.isActive(now)) {
            throw invalidRefreshToken();
        }

        token.revoke(now);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RANDOM_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidRefreshToken();
        }

        // Only the digest is persisted; the raw refresh token is shown once to the client.
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required for refresh token hashing", ex);
        }
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private UnauthorizedException invalidRefreshToken() {
        return new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
    }
}
