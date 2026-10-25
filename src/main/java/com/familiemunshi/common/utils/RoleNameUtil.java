package com.familiemunshi.common.utils;

import java.util.Locale;

public final class RoleNameUtil {
    private RoleNameUtil() {}


    private static String normalize(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Role name cannot be empty"
            );
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "_");

        if (!normalized.matches("[A-Z0-9_]+")) {
            throw new IllegalArgumentException(
                    "Role name contains invalid characters"
            );
        }

        return normalized;
    }

    public static String platformRole(String name) {

        String normalized = normalize(name);

        if (normalized.startsWith("SYS_")) {
            return normalized;
        }

        return "SYS_" + normalized;
    }

    public static String branchRole(
            String branchCode,
            String name
    ) {

        String code = normalize(branchCode);
        String roleName = normalize(name);

        String prefix = code + "_BRANCH_";

        if (roleName.startsWith(prefix)) {
            return roleName;
        }

        return prefix + roleName;
    }
}
