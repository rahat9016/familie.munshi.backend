package com.familiemunshi.service;


import com.familiemunshi.jpa.repositories.UserBranchRepository;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.service.enums.PermissionCode;
import com.familiemunshi.service.enums.RoleScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service("permissionAuthorizationService")
@RequiredArgsConstructor
public class PermissionAuthorizationService {
    private final UserPlatformRoleRepository userPlatformRoleRepository;
    private final UserBranchRepository userBranchRepository;
    private final UserBranchRoleRepository userBranchRoleRepository;

    public boolean hasPermission(
            Authentication authentication,
            String permissionCode
    ) {
        if (authentication == null ||
                !authentication.isAuthenticated()) {
            log.warn("Permission denied: unauthenticated");
            return false;
        }

        if (!PermissionCode.getSupportedPermissions()
                .contains(permissionCode)) {
            log.warn("Permission denied: unsupported code={}", permissionCode);
            return false;
        }

        UUID userId;

        try {
            userId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            log.warn("Permission denied: invalid principal UUID");
            return false;
        }

        boolean allowed = userPlatformRoleRepository.hasPermission(
                userId,
                RoleScope.PLATFORM,
                permissionCode
        );

        log.info(
                "Permission check: userId={}, code={}, allowed={}",
                userId,
                permissionCode,
                allowed
        );

        return allowed;
    }


    public boolean canReadBranch(Authentication authentication, UUID branchId) {
        if (hasPermission(authentication, "BRANCH_READ")) {
            return true;
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UUID userId;
        try {
            userId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            return false;
        }

        return userBranchRepository.existsByUser_IdAndBranch_Id(userId, branchId)
                && userBranchRoleRepository.hasBranchPermission(
                userId,
                branchId,
                RoleScope.BRANCH,
                "BRANCH_READ"
        );
    }
}
