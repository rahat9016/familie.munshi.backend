package com.familiemunshi.http.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@Setter
public class UpdateMyProfileRequest {

    @Schema(description = "First name. Omit or leave blank to keep current.", requiredMode = NOT_REQUIRED, example = "John")
    @Size(max = 120)
    private String firstName;

    @Schema(description = "Last name. Omit or leave blank to keep current.", requiredMode = NOT_REQUIRED, example = "Doe")
    @Size(max = 120)
    private String lastName;

    @Schema(description = "Phone number. Send blank to clear.", requiredMode = NOT_REQUIRED, example = "+1-234-567-8901")
    @Size(max = 20)
    private String phone;

    @Schema(description = "Profile picture image. Omit to keep current.", type = "string", format = "binary", requiredMode = NOT_REQUIRED)
    private MultipartFile profilePicture;
}
