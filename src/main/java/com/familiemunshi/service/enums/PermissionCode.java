package com.familiemunshi.service.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum PermissionCode {
    // ROLE MANAGEMENT
    ROLE_CREATE("ROLE_CREATE", "ROLE", "Create roles"),
    ROLE_READ("ROLE_READ", "ROLE", "View roles"),
    ROLE_UPDATE("ROLE_UPDATE", "ROLE", "Update roles"),
    ROLE_DELETE("ROLE_DELETE", "ROLE", "Delete roles"),

    // PERMISSION MANAGEMENT
    PERMISSION_READ(
            "PERMISSION_READ",
            "PERMISSION",
            "View permissions"
    ),
    ROLE_PERMISSION_MANAGE(
            "ROLE_PERMISSION_MANAGE",
            "PERMISSION",
            "Manage role permissions"
    ),

    // USER MANAGEMENT
    USER_CREATE("USER_CREATE", "USER", "Create users"),
    USER_READ("USER_READ", "USER", "View users"),
    USER_UPDATE("USER_UPDATE", "USER", "Update users"),
    USER_DELETE("USER_DELETE", "USER", "Delete users"),
    USER_ROLE_MANAGE(
            "USER_ROLE_MANAGE",
            "USER",
            "Manage user role assignments"
    ),

    // BRANCH MANAGEMENT
    BRANCH_CREATE("BRANCH_CREATE", "BRANCH", "Create branches"),
    BRANCH_READ("BRANCH_READ", "BRANCH", "View branches"),
    BRANCH_UPDATE("BRANCH_UPDATE", "BRANCH", "Update branches"),
    BRANCH_DELETE("BRANCH_DELETE", "BRANCH", "Delete branches"),

    // PRODUCT MANAGEMENT
    PRODUCT_CREATE("PRODUCT_CREATE", "PRODUCT", "Create products"),
    PRODUCT_READ("PRODUCT_READ", "PRODUCT", "View products"),
    PRODUCT_UPDATE("PRODUCT_UPDATE", "PRODUCT", "Update products"),
    PRODUCT_DELETE("PRODUCT_DELETE", "PRODUCT", "Delete products");


    private final String code;
    private final String module;
    private final String description;

    PermissionCode(
            String code,
            String module,
            String description
    ) {
        this.code = code;
        this.module = module;
        this.description = description;
    }

    /**
     * Returns the PermissionCode corresponding to the given code.
     *
     * @param code the permission code
     * @return the corresponding PermissionCode
     * @throws IllegalArgumentException if the code is invalid
     */
    public static PermissionCode fromCode(String code) {
        Objects.requireNonNull(code, "Permission code cannot be null");
        return Arrays
                .stream(PermissionCode.values())
                .filter(permission -> permission.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid permission code: " + code));
    }

    /**
     * Returns a list of all supported permission codes.
     *
     * @return a list of supported permission codes
     */
    public static List<String> getSupportedPermissions (){
        return Stream.of(PermissionCode.values())
                .map(PermissionCode::getCode)
                .toList();
    }

    /**
     * Checks if a given permission code is supported.
     *
     * @param code the permission code to check
     * @return true if the code is supported, false otherwise
     */
    public static boolean isSuppoerted(String code){
        return code != null && Stream
                .of(PermissionCode.values())
                .anyMatch(permission ->
                        permission.getCode()
                                .equalsIgnoreCase(code));
    }
}
