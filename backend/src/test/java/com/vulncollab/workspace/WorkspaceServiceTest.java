package com.vulncollab.workspace;

import com.vulncollab.common.error.ConflictException;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRepository;
import com.vulncollab.user.UserRole;
import com.vulncollab.workspace.dto.WorkspaceInviteRequest;
import com.vulncollab.workspace.dto.WorkspaceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceServiceTest {
    private final WorkspaceRepository workspaceRepository = mock(WorkspaceRepository.class);
    private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
    private final WorkspaceAccessService workspaceAccessService = mock(WorkspaceAccessService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final WorkspaceService workspaceService = new WorkspaceService(
            workspaceRepository,
            workspaceMemberRepository,
            workspaceAccessService,
            userRepository
    );

    @Test
    void listReturnsOnlyWorkspaceMembershipsProvidedByRepository() {
        User alice = user(2L, "alice@test.com");
        Workspace engineering = workspace(10L, "Engineering", alice);
        when(workspaceRepository.findAllForMember(2L)).thenReturn(List.of(engineering));
        when(workspaceAccessService.requireMember(engineering, alice))
                .thenReturn(new WorkspaceMember(engineering, alice, WorkspaceRole.OWNER, null));

        var result = workspaceService.list(alice);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Engineering");
        assertThat(result.getFirst().currentUserRole()).isEqualTo(WorkspaceRole.OWNER);
    }

    @Test
    void createWorkspaceAssignsCreatorAsOwner() {
        User alice = user(2L, "alice@test.com");
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace workspace = invocation.getArgument(0);
            ReflectionTestUtils.setField(workspace, "id", 10L);
            return workspace;
        });
        when(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = workspaceService.create(
                alice,
                new WorkspaceRequest("New Workspace", "  Planning  ", WorkspaceVisibility.PRIVATE)
        );

        assertThat(result.name()).isEqualTo("New Workspace");
        assertThat(result.description()).isEqualTo("Planning");
        assertThat(result.currentUserRole()).isEqualTo(WorkspaceRole.OWNER);
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void inviteAddsEnabledUserWhenRequesterIsOwner() {
        User alice = user(2L, "alice@test.com");
        User charlie = user(4L, "charlie@test.com");
        Workspace engineering = workspace(10L, "Engineering", alice);
        when(workspaceRepository.findByPublicId("wks-10")).thenReturn(Optional.of(engineering));
        when(userRepository.findByEmail("charlie@test.com")).thenReturn(Optional.of(charlie));
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(10L, 4L)).thenReturn(false);
        when(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = workspaceService.invite(
                alice,
                "wks-10",
                new WorkspaceInviteRequest("Charlie@Test.com", WorkspaceRole.VIEWER)
        );

        assertThat(result.email()).isEqualTo("charlie@test.com");
        assertThat(result.role()).isEqualTo(WorkspaceRole.VIEWER);
        verify(workspaceAccessService).requireOwner(engineering, alice);
    }

    @Test
    void inviteRejectsOwnerRole() {
        User alice = user(2L, "alice@test.com");
        Workspace engineering = workspace(10L, "Engineering", alice);
        when(workspaceRepository.findByPublicId("wks-10")).thenReturn(Optional.of(engineering));

        assertThatThrownBy(() -> workspaceService.invite(
                alice,
                "wks-10",
                new WorkspaceInviteRequest("bob@test.com", WorkspaceRole.OWNER)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Invite MEMBER or VIEWER; ownership transfer is not supported");
    }

    private User user(Long id, String email) {
        User user = new User("usr-" + id, email, "hash", "User " + id, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Workspace workspace(Long id, String name, User owner) {
        Workspace workspace = new Workspace(
                "wks-" + id,
                name,
                "Description",
                WorkspaceVisibility.PRIVATE,
                owner
        );
        ReflectionTestUtils.setField(workspace, "id", id);
        return workspace;
    }
}
