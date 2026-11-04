package com.familiemunshi.service;

import com.familiemunshi.common.policy.PermissionPolicy;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.daos.UserPlatformRoleDao;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.jpa.repositories.RoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.service.enums.RoleScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminBootstrapService {

    private static final String SUPER_ADMIN_ROLE =
            "SYS_SUPER_ADMIN";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPlatformRoleRepository userPlatformRoleRepository;

    public void initialize(
            String firstName,
            String lastName,
            String email,
            String rawPassword
    ) {

        // ==========================================
        // 1. VALIDATE BOOTSTRAP CONFIGURATION
        // ==========================================

        validateInput(
                firstName,
                lastName,
                email,
                rawPassword
        );

        // ==========================================
        // 2. LOAD PLATFORM PERMISSIONS ONLY
        // ==========================================

        List<String> supportedCodes =
                PermissionPolicy.getPlatformPermissions();

        List<PermissionDao> permissions =
                permissionRepository.findAllByNameIn(
                        supportedCodes
                );

        Set<String> foundCodes = permissions.stream()
                .map(PermissionDao::getName)
                .collect(Collectors.toSet());

        if (!foundCodes.containsAll(supportedCodes)) {
            throw new IllegalStateException(
                    "Platform permission seeding is incomplete"
            );
        }

        // ==========================================
        // 3. FIND OR CREATE SYS_SUPER_ADMIN ROLE
        // ==========================================

        RoleDao role = roleRepository
                .findByNameIgnoreCaseAndScope(
                        SUPER_ADMIN_ROLE,
                        RoleScope.PLATFORM
                )
                .orElseGet(() ->
                        RoleDao.builder()
                                .name(SUPER_ADMIN_ROLE)
                                .description(
                                        "Platform Super Administrator"
                                )
                                .scope(RoleScope.PLATFORM)
                                .branch(null)
                                .isSystem(true)
                                .isActive(true)
                                .build()
                );

        // DO NOT CONVERT A CUSTOM ROLE TO SYSTEM ROLE
        if (role.getId() != null
                && !Boolean.TRUE.equals(role.getIsSystem())) {

            throw new IllegalStateException(
                    "SYS_SUPER_ADMIN already exists "
                            + "but is not a system role"
            );
        }

        if (!Boolean.TRUE.equals(role.getIsActive())) {
            throw new IllegalStateException(
                    "SYS_SUPER_ADMIN role is inactive"
            );
        }

        if (role.getScope() != RoleScope.PLATFORM
                || role.getBranch() != null) {

            throw new IllegalStateException(
                    "SYS_SUPER_ADMIN must be platform-scoped"
            );
        }

        // ==========================================
        // 4. SYNC PLATFORM ROLE PERMISSIONS
        // ==========================================

        role.setPermissions(
                new HashSet<>(permissions)
        );

        RoleDao savedRole =
                roleRepository.saveAndFlush(role);

        // ==========================================
        // 5. FIND OR CREATE INITIAL ADMIN USER
        // ==========================================

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        UserDao user = userRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        boolean existingUser = user != null;

        if (user == null) {

            String salt = Util.generateSalt();

            String hashedPassword =
                    Util.hashPassword(rawPassword, salt);

            UserDao newUser = UserDao.builder()
                    .firstName(firstName.trim())
                    .lastName(lastName.trim())
                    .email(normalizedEmail)
                    .password(hashedPassword)
                    .salt(salt)
                    .isActive(true)
                    .build();

            user = userRepository.saveAndFlush(newUser);

            log.info(
                    "Initial Super Admin user created: {}",
                    user.getId()
            );
        }

        // ==========================================
        // 6. VALIDATE USER STATUS
        // ==========================================

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "Bootstrap Super Admin user is inactive"
            );
        }

        // ==========================================
        // 7. CHECK EXISTING ROLE ASSIGNMENT
        // ==========================================

        boolean alreadyAssigned =
                userPlatformRoleRepository
                        .existsByUser_IdAndRole_Id(
                                user.getId(),
                                savedRole.getId()
                        );

        // EXISTING NORMAL USER CANNOT BE PROMOTED
        if (existingUser && !alreadyAssigned) {
            throw new IllegalStateException(
                    "Bootstrap email already belongs to an "
                            + "ordinary user. Cannot promote automatically."
            );
        }

        // ==========================================
        // 8. ASSIGN PLATFORM ROLE
        // ==========================================

        if (!alreadyAssigned) {

            UserPlatformRoleDao assignment =
                    UserPlatformRoleDao.builder()
                            .user(user)
                            .role(savedRole)
                            .build();

            userPlatformRoleRepository.saveAndFlush(
                    assignment
            );

            log.info(
                    "SYS_SUPER_ADMIN assigned to user {}",
                    user.getId()
            );
        }

        log.info(
                "Super Admin bootstrap completed. "
                        + "Platform permissions synchronized: {}",
                permissions.size()
        );
    }

    private void validateInput(
            String firstName,
            String lastName,
            String email,
            String password
    ) {

        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalStateException(
                    "Super Admin bootstrap configuration is incomplete"
            );
        }

        if (password.length() < 12) {
            throw new IllegalStateException(
                    "Super Admin bootstrap password "
                            + "must contain at least 12 characters"
            );
        }
    }
}
