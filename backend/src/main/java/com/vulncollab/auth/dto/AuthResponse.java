package com.vulncollab.auth.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        String refreshToken,
        Instant refreshExpiresAt,
        UserSummary user
) {
}
