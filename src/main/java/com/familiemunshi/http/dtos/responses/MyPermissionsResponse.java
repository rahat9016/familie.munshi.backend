package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.enums.RoleScope;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyPermissionsResponse(
        UUID userId,
        RoleScope scope,
        UUID branchId,
        String branchCode,
        String branchName,
        List<String> roles,
        List<ModulePermissionTreeResponse> permissions,
        List<BranchPermissionResponse> branches
) {

    public static MyPermissionsResponse platform(
            UUID userId,
            List<String> roles,
            List<ModulePermissionTreeResponse> permissions
    ) {
        return new MyPermissionsResponse(
                userId,
                RoleScope.PLATFORM,
                null,
                null,
                null,
                roles,
                permissions,
                null
        );
    }

    public static MyPermissionsResponse branch(
            UUID userId,
            BranchPermissionResponse branch
    ) {
        return new MyPermissionsResponse(
                userId,
                RoleScope.BRANCH,
                branch.branchId(),
                branch.branchCode(),
                branch.branchName(),
                branch.roles(),
                branch.permissions(),
                null
        );
    }

    public static MyPermissionsResponse multipleBranches(
            UUID userId,
            List<BranchPermissionResponse> branches
    ) {
        return new MyPermissionsResponse(
                userId,
                RoleScope.BRANCH,
                null,
                null,
                null,
                null,
                null,
                branches
        );
    }
}
