package com.vulncollab.workspace.dto;

import com.vulncollab.workspace.WorkspaceVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(
        @NotBlank
        @Size(min = 2, max = 160)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        WorkspaceVisibility visibility
) {
}
