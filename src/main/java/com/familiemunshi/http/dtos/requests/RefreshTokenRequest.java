package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class RefreshTokenRequest {
    @Schema(description = "Refresh token for the user", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    @NotBlank(message = "Refresh token is required")
    String refreshToken;
}
