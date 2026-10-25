package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.service.entities.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
public class CreateRoleRequest {
    @NotBlank(message = "Role name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Role name must be between 2 and 100 characters"
    )
    private String name;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters"
    )
    private String description;

    public Role toEntity() {
        return Role.builder()
                .name(this.name.trim())
                .description(this.description != null ? this.description.trim() : null)
                .build();
    }
}
