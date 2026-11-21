package com.familiemunshi.service.filters;

import com.familiemunshi.service.enums.ProductClassificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAllProductClassificationsFilter extends BaseFilter {

    @Schema(
            description = "Filter by classification level",
            example = "CATEGORY"
    )
    private ProductClassificationLevel level;
}
