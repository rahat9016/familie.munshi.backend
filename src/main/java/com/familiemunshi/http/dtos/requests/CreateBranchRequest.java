package com.familiemunshi.http.dtos.requests;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class CreateBranchRequest {
    @Schema(description = "Name of the branch", example = "Mirpur Branch", requiredMode = REQUIRED)
    @NotBlank(message="Branch name is required")
    @Min(2)
    private String name;

    @Schema(description = "Address of the branch", example = "Mirpur-10, Dhaka", requiredMode = NOT_REQUIRED)
    private String address;

    @Schema(description = "Branch active status", example = "true", defaultValue = "true", requiredMode = NOT_REQUIRED)
    private Boolean isActive = true;

    @Schema(description = "Branch logo image file", type = "string", format = "binary", requiredMode = NOT_REQUIRED)
    private MultipartFile logoUrl;
}
