package com.familiemunshi.service;


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
}
