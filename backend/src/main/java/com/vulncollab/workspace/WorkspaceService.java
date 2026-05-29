package com.vulncollab.workspace;

import com.vulncollab.common.error.ConflictException;
import com.vulncollab.common.error.NotFoundException;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRepository;
import com.vulncollab.workspace.dto.WorkspaceInviteRequest;
import com.vulncollab.workspace.dto.WorkspaceMemberResponse;
import com.vulncollab.workspace.dto.WorkspaceRequest;
import com.vulncollab.workspace.dto.WorkspaceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final UserRepository userRepository;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            WorkspaceAccessService workspaceAccessService,
            UserRepository userRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> list(User user) {
        return workspaceRepository.findAllForMember(user.getId()).stream()
                .map(workspace -> {
                    WorkspaceRole role = workspaceAccessService.requireMember(workspace, user).getRole();
                    return WorkspaceResponse.from(workspace, role);
                })
                .toList();
    }

    @Transactional
    public WorkspaceResponse create(User user, WorkspaceRequest request) {
        Workspace workspace = workspaceRepository.save(new Workspace(
                generatePublicId(),
                request.name().trim(),
                cleanDescription(request.description()),
                request.visibility(),
                user
        ));

        // Creators become owners immediately so all later authorization uses membership, not owner_id alone.
        WorkspaceMember ownerMembership = workspaceMemberRepository.save(new WorkspaceMember(
                workspace,
                user,
                WorkspaceRole.OWNER,
                null
        ));

        return WorkspaceResponse.from(workspace, ownerMembership.getRole());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse detail(User user, String publicId) {
        Workspace workspace = findByPublicId(publicId);
        WorkspaceRole role = workspaceAccessService.requireMember(workspace, user).getRole();
        return WorkspaceResponse.from(workspace, role);
    }

    @Transactional
    public WorkspaceResponse update(User user, String publicId, WorkspaceRequest request) {
        Workspace workspace = findByPublicId(publicId);
        WorkspaceRole role = workspaceAccessService.requireOwner(workspace, user).getRole();

        workspace.update(request.name().trim(), cleanDescription(request.description()), request.visibility());
        return WorkspaceResponse.from(workspace, role);
    }

    @Transactional
    public WorkspaceMemberResponse invite(User inviter, String publicId, WorkspaceInviteRequest request) {
        Workspace workspace = findByPublicId(publicId);
        workspaceAccessService.requireOwner(workspace, inviter);

        if (request.role() == WorkspaceRole.OWNER) {
            throw new ConflictException("OWNER_INVITE_NOT_ALLOWED", "Invite MEMBER or VIEWER; ownership transfer is not supported");
        }

        User invitedUser = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(User::isEnabled)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User was not found"));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), invitedUser.getId())) {
            throw new ConflictException("WORKSPACE_MEMBER_EXISTS", "User is already a workspace member");
        }

        WorkspaceMember member = workspaceMemberRepository.save(new WorkspaceMember(
                workspace,
                invitedUser,
                request.role(),
                inviter
        ));
        return WorkspaceMemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> members(User user, String publicId) {
        Workspace workspace = findByPublicId(publicId);
        workspaceAccessService.requireOwner(workspace, user);

        return workspaceMemberRepository.findAllByWorkspaceIdOrderByJoinedAtAsc(workspace.getId()).stream()
                .map(WorkspaceMemberResponse::from)
                .toList();
    }

    private Workspace findByPublicId(String publicId) {
        return workspaceRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("WORKSPACE_NOT_FOUND", "Workspace was not found"));
    }

    private String generatePublicId() {
        String publicId;
        do {
            publicId = UUID.randomUUID().toString();
        } while (workspaceRepository.existsByPublicId(publicId));
        return publicId;
    }

    private String cleanDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
