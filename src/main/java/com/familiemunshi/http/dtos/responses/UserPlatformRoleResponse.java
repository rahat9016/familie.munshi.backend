package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.UserPlatformRole;

import java.util.UUID;

public record UserPlatformRoleResponse(
        UUID userId,
        UUID roleId
) {

    public UserPlatformRoleResponse(UserPlatformRole mapping) {
        this(
                mapping.userId(),
                mapping.roleId()
        );
    }
}