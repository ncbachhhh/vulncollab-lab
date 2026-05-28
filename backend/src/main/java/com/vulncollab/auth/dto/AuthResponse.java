package com.vulncollab.auth.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        UserSummary user
) {
}
