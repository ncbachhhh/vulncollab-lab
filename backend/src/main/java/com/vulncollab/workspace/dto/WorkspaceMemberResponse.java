package com.vulncollab.workspace.dto;

import com.vulncollab.workspace.WorkspaceMember;
import com.vulncollab.workspace.WorkspaceRole;

import java.time.Instant;

public record WorkspaceMemberResponse(
        String userPublicId,
        String email,
        String displayName,
        WorkspaceRole role,
        String invitedByPublicId,
        Instant joinedAt
) {
    public static WorkspaceMemberResponse from(WorkspaceMember member) {
        return new WorkspaceMemberResponse(
                member.getUser().getPublicId(),
                member.getUser().getEmail(),
                member.getUser().getDisplayName(),
                member.getRole(),
                member.getInvitedBy() == null ? null : member.getInvitedBy().getPublicId(),
                member.getJoinedAt()
        );
    }
}
