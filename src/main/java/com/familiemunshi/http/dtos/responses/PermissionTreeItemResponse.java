package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Permission;

import java.util.UUID;

public record PermissionTreeItemResponse(
        UUID id,
        String name,
        String description
) {
    public PermissionTreeItemResponse(Permission permission) {
        this(
                permission.id(),
                permission.code().getCode(),
                permission.code().getDescription()
        );
    }
}
