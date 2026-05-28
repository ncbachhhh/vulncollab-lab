package com.vulncollab.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 2, max = 120)
        String displayName,

        @NotBlank
        @Size(min = 8, max = 128)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
                message = "must contain uppercase, lowercase, and number characters"
        )
        String password
) {
}
