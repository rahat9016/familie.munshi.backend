package com.familiemunshi.http.dtos.requests;


import com.familiemunshi.service.entities.RolePermissions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class AssignRolePermissionsRequest {
    @NotEmpty(message = "At least one permission is required")
    @Size(
            max = 100,
            message = "A maximum of 100 permissions is allowed"
    )
    private Set<
            @NotBlank(message = "Permission code cannot be blank")
            @Size(
                    max = 100,
                    message = "Permission code cannot exceed 100 characters"
            )
                    String
            > permissionCodes;

    public RolePermissions toEntity(UUID roleId) {
        Set<String> normalizedCodes = permissionCodes.stream()
                .map(String::trim)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return new RolePermissions(
                roleId,
                normalizedCodes
        );
    }
}
