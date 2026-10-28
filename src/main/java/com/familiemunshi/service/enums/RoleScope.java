package com.familiemunshi.service.enums;


import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum RoleScope {
    PLATFORM("Platform"),
    BRANCH("Branch");

    private final String code;

    RoleScope(String code) {
        this.code = code;
    }

    public static RoleScope fromCode(String code) {
        Objects.requireNonNull(code, "Role scope cannot be null");

        return Arrays.stream(RoleScope.values())
                .filter(scope ->
                        scope.getCode().equalsIgnoreCase(code)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid role scope: " + code
                        )
                );
    }

    public static List<String> getSupportedRoleScopes() {
        return Stream.of(RoleScope.values())
                .map(RoleScope::getCode)
                .toList();
    }
}
