package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.jpa.daos.UserDao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
public class SignupRequest {
    @Schema(description = "First name of the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "John")
    @NotBlank(message = "First name is required")
    @Size(max = 255, min = 1, message = "First name cannot exceed 255 characters")
    private String firstName;

    @Schema(description = "Last name of the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(max = 255, min = 1, message = "Last name cannot exceed 255 characters")
    private String lastName;

    @Schema(description = "Email address of the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Size(max = 255, min = 1, message = "Email cannot exceed 255 characters")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Password for the user account", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "Password is required")
    @Size(max = 255, min = 6, message = "Password must be at least 6 characters long")
    private String password;

    public UserDao toEntity() {
        return UserDao.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .password(this.password)
                .isActive(true)
                .build();
    }
}
