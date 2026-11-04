
package com.familiemunshi.http.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateBranchAdminRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "New password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Old password must contain 8 to 72 characters"
        )
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(
                min = 8,
                max = 72,
                message = "New password must contain 8 to 72 characters"
        )
        String newPassword
) {
}
