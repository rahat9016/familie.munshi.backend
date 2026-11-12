package com.familiemunshi.http.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RolePermissionsResponse {
    UUID roleId;
    List<ModulePermissionTreeResponse> permissions;

    public RolePermissionsResponse(UUID roleId, List<ModulePermissionTreeResponse> permissions) {
        this.roleId = roleId;
        this.permissions = permissions;
    }
}
