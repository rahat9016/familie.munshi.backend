package com.familiemunshi.service.entities;

import java.util.Set;
import java.util.UUID;

public record RolePermissions(UUID roleId, Set<String> permissionCodes) {
    public RolePermissions {
        permissionCodes = Set.copyOf(permissionCodes);
    }
}
