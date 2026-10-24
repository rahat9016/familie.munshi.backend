package com.familiemunshi.http.dtos.requests;

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
}
