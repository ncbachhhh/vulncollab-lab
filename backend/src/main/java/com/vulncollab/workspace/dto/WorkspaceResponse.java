package com.vulncollab.workspace.dto;

import com.vulncollab.workspace.Workspace;
import com.vulncollab.workspace.WorkspaceRole;
import com.vulncollab.workspace.WorkspaceVisibility;

import java.time.Instant;

public record WorkspaceResponse(
        String publicId,
        String name,
        String description,
        WorkspaceVisibility visibility,
        String ownerPublicId,
        WorkspaceRole currentUserRole,
        Instant createdAt,
        Instant updatedAt
) {
    public static WorkspaceResponse from(Workspace workspace, WorkspaceRole currentUserRole) {
        return new WorkspaceResponse(
                workspace.getPublicId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getVisibility(),
                workspace.getOwner().getPublicId(),
                currentUserRole,
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}
