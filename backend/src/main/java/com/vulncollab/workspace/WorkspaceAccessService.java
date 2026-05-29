package com.vulncollab.workspace;

import com.vulncollab.common.error.ForbiddenException;
import com.vulncollab.common.error.NotFoundException;
import com.vulncollab.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceAccessService {
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceAccessService(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Transactional(readOnly = true)
    public WorkspaceMember requireMember(Workspace workspace, User user) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                // Returning 404 avoids confirming that a private workspace exists.
                .orElseThrow(() -> new NotFoundException("WORKSPACE_NOT_FOUND", "Workspace was not found"));
    }

    @Transactional(readOnly = true)
    public WorkspaceMember requireOwner(Workspace workspace, User user) {
        WorkspaceMember member = requireMember(workspace, user);
        if (member.getRole() != WorkspaceRole.OWNER) {
            throw new ForbiddenException("WORKSPACE_OWNER_REQUIRED", "Workspace owner role is required");
        }
        return member;
    }
}
