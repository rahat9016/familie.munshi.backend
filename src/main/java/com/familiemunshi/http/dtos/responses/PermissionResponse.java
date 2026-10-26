package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Permission;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name,
        String module,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public PermissionResponse(Permission permission) {
        this(
                permission.id(),
                permission.code().getCode(),
                permission.code().getModule(),
                permission.code().getDescription(),
                permission.createdAt(),
                permission.updatedAt()
        );
    }
}
