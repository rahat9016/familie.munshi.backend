package com.familiemunshi.http.dtos.requests;


import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.service.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @Schema(description = "First name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 120)
    private String firstName;

    @Schema(description = "Last name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 120)
    private String lastName;

    @Schema(description = "Email address", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150)
    private String email;

    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255)
    private String password;

    @Schema(description = "Phone number")
    @Size(max = 20)
    private String phone;

    @Schema(description = "Profile picture", type = "string", format = "binary")
    private MultipartFile profilePicture;

    @Builder.Default
    private Boolean isActive = true;

    @Schema(
            description = "Role ID required for PLATFORM and BRANCH roles",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @Schema(description = "Branch ID required for BRANCH roles")
    private UUID branchId;

    public User toEntity() {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .isActive(isActive == null || isActive)
                .build();
    }
}
