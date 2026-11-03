
package com.familiemunshi.http.dtos.requests;

import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.service.entities.Branch;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateBranchRequest {

    @Size(max = 255)
    private String name;

    private String address;

    @Size(max = 20)
    private String phone;

    private Boolean isActive;

    private MultipartFile logo;

    public Branch toEntity(Branch existingBranch) {

        validateProperties();

        Branch updatedBranch = existingBranch;

        if (name != null) {
            updatedBranch = updatedBranch.withName(name.trim());
        }

        if (address != null) {
            updatedBranch = updatedBranch.withAddress(address);
        }

        if (phone != null) {
            updatedBranch = updatedBranch.withPhone(phone);
        }

        if (isActive != null) {
            updatedBranch = updatedBranch.withActive(isActive);
        }

        return updatedBranch;
    }

    private void validateProperties() {

        if (name != null && name.isBlank()) {
            throw new BadRequestException(
                    "branchNameCannotBeBlank"
            );
        }

        if (logo != null) {
            ValidationUtil.validateMediaType(logo);
        }
    }
}
