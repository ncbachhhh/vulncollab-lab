package com.vulncollab.auth.dto;

import com.vulncollab.user.User;
import com.vulncollab.user.UserRole;

public record UserSummary(
        String publicId,
        String email,
        String displayName,
        UserRole role
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getPublicId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}
