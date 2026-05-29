package com.vulncollab.workspace;

import com.vulncollab.common.error.ForbiddenException;
import com.vulncollab.common.error.NotFoundException;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceAccessServiceTest {
    private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
    private final WorkspaceAccessService workspaceAccessService = new WorkspaceAccessService(workspaceMemberRepository);

    @Test
    void requireMemberReturnsMembershipWhenUserBelongsToWorkspace() {
        User user = user(2L, "alice@test.com");
        Workspace workspace = workspace(10L, user);
        WorkspaceMember member = new WorkspaceMember(workspace, user, WorkspaceRole.MEMBER, null);
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(10L, 2L)).thenReturn(Optional.of(member));

        WorkspaceMember result = workspaceAccessService.requireMember(workspace, user);

        assertThat(result.getRole()).isEqualTo(WorkspaceRole.MEMBER);
    }

    @Test
    void requireMemberHidesPrivateWorkspaceWhenMembershipIsMissing() {
        User user = user(3L, "bob@test.com");
        Workspace workspace = workspace(10L, user(2L, "alice@test.com"));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(10L, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceAccessService.requireMember(workspace, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Workspace was not found");
    }

    @Test
    void requireOwnerRejectsNonOwnerMembers() {
        User user = user(3L, "bob@test.com");
        Workspace workspace = workspace(10L, user(2L, "alice@test.com"));
        WorkspaceMember member = new WorkspaceMember(workspace, user, WorkspaceRole.VIEWER, null);
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(10L, 3L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> workspaceAccessService.requireOwner(workspace, user))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Workspace owner role is required");
    }

    private User user(Long id, String email) {
        User user = new User("usr-" + id, email, "hash", "User " + id, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Workspace workspace(Long id, User owner) {
        Workspace workspace = new Workspace(
                "wks-" + id,
                "Workspace " + id,
                "Description",
                WorkspaceVisibility.PRIVATE,
                owner
        );
        ReflectionTestUtils.setField(workspace, "id", id);
        return workspace;
    }
}
