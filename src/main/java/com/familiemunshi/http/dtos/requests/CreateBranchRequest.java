package com.familiemunshi.http.dtos.requests;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.familiemunshi.jpa.daos.BranchDao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateBranchRequest {
    @Schema(description = "Branch name", requiredMode = REQUIRED, example = "Downtown Branch")
    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name cannot exceed 255 characters")
    private String name;

    @Schema(description = "Branch code", requiredMode = REQUIRED, example = "DB001")
    @NotBlank(message = "Branch code is required")
    @Size(max = 100, message = "Branch code cannot exceed 100 characters")
    private String code;

    @Schema(description = "Branch address", requiredMode = NOT_REQUIRED, example = "123 Main St, Cityville")
    private String address;

    @Schema(description = "Branch phone number", requiredMode = REQUIRED, example = "+1-234-567-8901")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Schema(
            description = "Branch logo",
            type = "string",
            format = "binary"
    )
    private MultipartFile logo;

    @Schema(description = "Indicates if the branch is active", requiredMode = NOT_REQUIRED, example = "true")
    private Boolean isActive;

    public BranchDao toEntity() {
        return BranchDao.builder()
                .name(this.name)
                .code(this.code)
                .address(this.address)
                .phone(this.phone)
                .isActive(this.isActive)
                .build();
    }

}
