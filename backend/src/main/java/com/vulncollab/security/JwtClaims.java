package com.vulncollab.security;

import com.vulncollab.user.UserRole;

import java.time.Instant;

public record JwtClaims(
        String subject,
        UserRole role,
        Instant issuedAt,
        Instant expiresAt
) {
}
