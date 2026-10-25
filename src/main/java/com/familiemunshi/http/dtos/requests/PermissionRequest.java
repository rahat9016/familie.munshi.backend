package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PermissionRequest {
    @Schema(description = "Name of the permission", requiredMode = Schema.RequiredMode.REQUIRED, example = "READ_PRIVILEGES")
    @NotBlank(message = "Permission name is required")
    @Size(min = 1, max = 255, message = "Permission name must not exceed 255 characters")
    private String name;

    public Permission toEntity() {
        return Permission.builder()
                .name(this.name)
                .build();
    }
}
