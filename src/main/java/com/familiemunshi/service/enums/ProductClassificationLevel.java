
package com.familiemunshi.service.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum ProductClassificationLevel {

    /*
     * Hierarchy order. SEGMENT is optional:
     * GROUP -> PARENT_CATEGORY -> [SEGMENT] -> CATEGORY -> SUB_CATEGORY
     */
    GROUP("Top Level Group"),
    PARENT_CATEGORY("Group's Parent Category"),
    SEGMENT("Parent Category's Segment"),
    CATEGORY("Segment's Category"),
    SUB_CATEGORY("Category's Sub Category");

    private final String code;

    ProductClassificationLevel(String code) {
        this.code = code;
    }

    public static ProductClassificationLevel fromCode(String code) {
        Objects.requireNonNull(
                code,
                "Product classification level cannot be null"
        );

        return Arrays.stream(ProductClassificationLevel.values())
                .filter(level ->
                        level.getCode().equalsIgnoreCase(code)
                                || level.name().equalsIgnoreCase(code)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid product classification level: " + code
                        )
                );
    }

    public static List<String> getSupportedClassificationLevels() {
        return Stream.of(ProductClassificationLevel.values())
                .map(ProductClassificationLevel::getCode)
                .toList();
    }
}
