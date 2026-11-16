
package com.familiemunshi.service.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum ClassificationType {

    GROUP("Group"),
    PARENT_CATEGORY("Parent Category"),
    CATEGORY("Category"),
    SEGMENT("Segment"),
    SUB_CATEGORY("Sub Category");

    private final String code;

    ClassificationType(String code) {
        this.code = code;
    }

    public static ClassificationType fromCode(String code) {
        Objects.requireNonNull(code, "Classification type cannot be null");

        return Arrays.stream(ClassificationType.values())
                .filter(type ->
                        type.getCode().equalsIgnoreCase(code)
                                || type.name().equalsIgnoreCase(code)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid classification type: " + code
                        )
                );
    }

    public static List<String> getSupportedClassificationTypes() {
        return Stream.of(ClassificationType.values())
                .map(ClassificationType::getCode)
                .toList();
    }
}
