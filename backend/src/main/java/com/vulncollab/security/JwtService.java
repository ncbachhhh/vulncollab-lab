package com.vulncollab.security;

import com.vulncollab.common.error.BadRequestException;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.time.Instant;
import java.util.Optional;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = hmacSha256Key(jwtProperties.secret());
    }

    public Instant accessTokenExpiresAt() {
        return Instant.now().plusMillis(jwtProperties.accessTokenExpirationMs());
    }

    public String createAccessToken(User user, Instant expiresAt) {
        Instant issuedAt = Instant.now();

        return Jwts.builder()
                .subject(user.getPublicId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Optional<JwtClaims> validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claimsFrom(claims);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private Optional<JwtClaims> claimsFrom(Claims claims) {
        String subject = claims.getSubject();
        String roleName = claims.get("role", String.class);
        Date issuedAt = claims.getIssuedAt();
        Date expiresAt = claims.getExpiration();

        if (subject == null || roleName == null || issuedAt == null || expiresAt == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new JwtClaims(
                    subject,
                    UserRole.valueOf(roleName),
                    issuedAt.toInstant(),
                    expiresAt.toInstant()
            ));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private SecretKey hmacSha256Key(String secret) {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (NoSuchAlgorithmException ex) {
            throw new BadRequestException("JWT_SIGNING_KEY_FAILED", "Failed to prepare JWT signing key");
        }
    }
}
