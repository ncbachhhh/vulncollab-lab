package com.vulncollab.workspace;

import com.vulncollab.common.api.ApiResponse;
import com.vulncollab.security.UserPrincipal;
import com.vulncollab.workspace.dto.WorkspaceInviteRequest;
import com.vulncollab.workspace.dto.WorkspaceMemberResponse;
import com.vulncollab.workspace.dto.WorkspaceRequest;
import com.vulncollab.workspace.dto.WorkspaceResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    ApiResponse<List<WorkspaceResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(workspaceService.list(principal.user()));
    }

    @PostMapping
    ApiResponse<WorkspaceResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WorkspaceRequest request
    ) {
        return ApiResponse.success(workspaceService.create(principal.user(), request));
    }

    @GetMapping("/{publicId}")
    ApiResponse<WorkspaceResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String publicId
    ) {
        return ApiResponse.success(workspaceService.detail(principal.user(), publicId));
    }

    @PatchMapping("/{publicId}")
    ApiResponse<WorkspaceResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String publicId,
            @Valid @RequestBody WorkspaceRequest request
    ) {
        return ApiResponse.success(workspaceService.update(principal.user(), publicId, request));
    }

    @PostMapping("/{publicId}/invite")
    ApiResponse<WorkspaceMemberResponse> invite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String publicId,
            @Valid @RequestBody WorkspaceInviteRequest request
    ) {
        return ApiResponse.success(workspaceService.invite(principal.user(), publicId, request));
    }

    @GetMapping("/{publicId}/members")
    ApiResponse<List<WorkspaceMemberResponse>> members(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String publicId
    ) {
        return ApiResponse.success(workspaceService.members(principal.user(), publicId));
    }
}
