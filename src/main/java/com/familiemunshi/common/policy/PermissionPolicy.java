
package com.familiemunshi.common.policy;

import com.familiemunshi.service.enums.PermissionCode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class PermissionPolicy {

    private PermissionPolicy() {
        throw new IllegalStateException(
                "Utility class cannot be instantiated"
        );
    }

    // ========================================
    // PLATFORM SUPER ADMIN PERMISSIONS
    // ========================================

    private static final Set<PermissionCode> PLATFORM =
            EnumSet.of(
                    PermissionCode.BRANCH_CREATE,
                    PermissionCode.BRANCH_READ,
                    PermissionCode.BRANCH_LIST,
                    PermissionCode.BRANCH_UPDATE,
                    PermissionCode.BRANCH_DELETE,

                    PermissionCode.ROLE_CREATE,
                    PermissionCode.ROLE_READ,
                    PermissionCode.ROLE_UPDATE,
                    PermissionCode.ROLE_DELETE,

                    PermissionCode.PERMISSION_READ,
                    PermissionCode.ROLE_PERMISSION_MANAGE,

                    PermissionCode.USER_CREATE,
                    PermissionCode.USER_READ,
                    PermissionCode.USER_UPDATE,
                    PermissionCode.USER_DELETE,
                    PermissionCode.USER_ROLE_MANAGE
            );

    // ========================================
    // DEFAULT BRANCH ADMIN PERMISSIONS
    // ========================================

    private static final Set<PermissionCode> BRANCH =
            EnumSet.of(
                    // BRANCH MANAGEMENT
                    PermissionCode.BRANCH_READ,
                    PermissionCode.BRANCH_UPDATE,

                    // USER MANAGEMENT
                    PermissionCode.USER_CREATE,
                    PermissionCode.USER_READ,
                    PermissionCode.USER_UPDATE,
                    PermissionCode.USER_DELETE,
                    PermissionCode.USER_ROLE_MANAGE,

                    // ROLE MANAGEMENT
                    PermissionCode.ROLE_CREATE,
                    PermissionCode.ROLE_READ,
                    PermissionCode.ROLE_UPDATE,
                    PermissionCode.ROLE_DELETE,

                    // PERMISSION MANAGEMENT
                    PermissionCode.PERMISSION_READ,
                    PermissionCode.ROLE_PERMISSION_MANAGE,

                    // PRODUCT MANAGEMENT
                    PermissionCode.PRODUCT_CREATE,
                    PermissionCode.PRODUCT_READ,
                    PermissionCode.PRODUCT_UPDATE,
                    PermissionCode.PRODUCT_DELETE,

                    // SALES MANAGEMENT
                    PermissionCode.SALE_CREATE,
                    PermissionCode.SALE_READ,
                    PermissionCode.SALE_UPDATE,
                    PermissionCode.SALE_DELETE,
                    PermissionCode.SALE_REFUND,

                    // ORDER MANAGEMENT
                    PermissionCode.ORDER_CREATE,
                    PermissionCode.ORDER_READ,
                    PermissionCode.ORDER_UPDATE,
                    PermissionCode.ORDER_CANCEL
            );

    // ========================================
    // GET PLATFORM PERMISSION CODES
    // ========================================

    public static List<String> getPlatformPermissions() {
        return PLATFORM.stream()
                .map(PermissionCode::getCode)
                .sorted()
                .toList();
    }

    // ========================================
    // GET BRANCH PERMISSION CODES
    // ========================================

    public static List<String> getBranchPermissions() {
        return BRANCH.stream()
                .map(PermissionCode::getCode)
                .sorted()
                .toList();
    }

    // ========================================
    // CHECK PLATFORM PERMISSION
    // ========================================

    public static boolean isPlatformPermission(String code) {
        return PermissionCode.isSuppoerted(code)
                && PLATFORM.contains(
                PermissionCode.fromCode(code)
        );
    }

    // ========================================
    // CHECK BRANCH PERMISSION
    // ========================================

    public static boolean isBranchPermission(String code) {
        return PermissionCode.isSuppoerted(code)
                && BRANCH.contains(
                PermissionCode.fromCode(code)
        );
    }
}
