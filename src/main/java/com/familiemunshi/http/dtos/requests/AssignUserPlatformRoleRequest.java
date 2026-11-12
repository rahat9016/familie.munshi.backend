package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.UserPlatformRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class AssignUserPlatformRoleRequest {

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    public UserPlatformRole toEntity(UUID userId) {
        return new UserPlatformRole(
                userId,
                roleId
        );
    }
}