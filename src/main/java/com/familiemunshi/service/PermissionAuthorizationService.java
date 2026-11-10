package com.familiemunshi.service;


import com.familiemunshi.common.policy.PermissionPolicy;
import com.familiemunshi.jpa.repositories.UserBranchRepository;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.service.enums.PermissionCode;
import com.familiemunshi.service.enums.RoleScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
        UUID userId = getAuthenticatedUserId(authentication);

        if (userId == null) {
            return false;
        }

        if (permissionCode == null
                || !PermissionCode.getSupportedPermissions().contains(permissionCode)) {
            log.warn("Permission denied: unsupported code={}", permissionCode);
            return false;
        }

        return userPlatformRoleRepository.hasPermission(
                userId,
                RoleScope.PLATFORM,
                permissionCode
        );
    }


    public boolean canReadBranch(
            Authentication authentication,
            UUID branchId
    ) {
        return hasBranchPermission(authentication, branchId, "BRANCH_READ");
    }

    public boolean hasBranchPermission(
            Authentication authentication,
            UUID branchId,
            String permissionCode
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return hasBranchPermission(userId, branchId, permissionCode);
    }

    public void requireBranchPermission(
            UUID userId,
            UUID branchId,
            String permissionCode
    ) {
        if (!hasBranchPermission(userId, branchId, permissionCode)) {
            throw new AccessDeniedException("Permission denied for this branch");
        }
    }

    private boolean hasBranchPermission(
            UUID userId,
            UUID branchId,
            String permissionCode
    ) {
        if (userId == null || branchId == null || permissionCode == null) {
            return false;
        }

        if (PermissionPolicy.isPlatformPermission(permissionCode)
                && userPlatformRoleRepository.hasPermission(
                userId,
                RoleScope.PLATFORM,
                permissionCode
        )) {
            return true;
        }

        return PermissionPolicy.isBranchPermission(permissionCode)
                && userBranchRepository.existsByUser_IdAndBranch_Id(
                userId,
                branchId
        )
                && userBranchRoleRepository.hasBranchPermission(
                userId,
                branchId,
                RoleScope.BRANCH,
                permissionCode
        );
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String principal = authentication.getName();

        if (principal == null) {
            return null;
        }

        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException exception) {
            log.debug("Permission denied: principal is not a valid UUID");
            return null;
        }
    }
}
