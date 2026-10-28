package com.familiemunshi.service.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAllRolesFilter extends BaseFilter {
    @Schema(
            description = "Filter system or custom roles",
            example = "false"
    )
    private Boolean isSystem;
}
