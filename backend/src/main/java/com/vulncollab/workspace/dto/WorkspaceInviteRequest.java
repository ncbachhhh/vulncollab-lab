package com.vulncollab.workspace.dto;

import com.vulncollab.workspace.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkspaceInviteRequest(
        @NotBlank
        @Email
        String email,

        @NotNull
        WorkspaceRole role
) {
}
