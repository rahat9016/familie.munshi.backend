package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.daos.UserPlatformRoleDao;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.jpa.repositories.RoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.service.enums.PermissionCode;
import com.familiemunshi.service.enums.RoleScope;
import jakarta.transaction.Transactional;
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


    @Transactional
    public void initialize(
            String firstName,
            String lastName,
            String email,
            String rawPassword
    ) {

        validateInput(firstName, lastName, email, rawPassword);

        // 1. Resolve supported permissions
        List<String> supportedCodes =
                PermissionCode.getSupportedPermissions();

        List<PermissionDao> permissions =
                permissionRepository.findAllByNameIn(supportedCodes);

        Set<String> foundCodes = permissions.stream()
                .map(PermissionDao::getName)
                .collect(Collectors.toSet());

        if (!foundCodes.containsAll(supportedCodes)) {
            throw new IllegalStateException(
                    "Permission seeding is incomplete"
            );
        }

        // 2. Find or create protected Super Admin role
        RoleDao role = roleRepository
                .findByNameIgnoreCaseAndScope(
                        SUPER_ADMIN_ROLE,
                        RoleScope.PLATFORM
                )
                .orElseGet(() -> RoleDao.builder()
                        .name(SUPER_ADMIN_ROLE)
                        .description("Platform Super Administrator")
                        .scope(RoleScope.PLATFORM)
                        .isSystem(true)
                        .isActive(true)
                        .build()
                );

        // Never turn an existing custom role into a system role
        if (role.getId() != null
                && !Boolean.TRUE.equals(role.getIsSystem())) {
            throw new IllegalStateException(
                    "SYS_SUPER_ADMIN exists but is not a system role"
            );
        }

        if (!Boolean.TRUE.equals(role.getIsActive())) {
            throw new IllegalStateException(
                    "SYS_SUPER_ADMIN role is inactive"
            );
        }

        // 3. Synchronize all supported permissions
        role.setPermissions(new HashSet<>(permissions));

        RoleDao savedRole = roleRepository.save(role);

        // 4. Find or create bootstrap user
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        UserDao user = userRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        boolean existingUser = user != null;

        if (!existingUser) {

            String salt = Util.generateSalt();

            String hashedPassword = Util.hashPassword(
                    rawPassword,
                    salt
            );

            UserDao newUser = UserDao.builder()
                    .firstName(firstName.trim())
                    .lastName(lastName.trim())
                    .email(normalizedEmail)
                    .password(hashedPassword)
                    .salt(salt)
                    .isActive(true)
                    .build();

            user = userRepository.save(newUser);

            log.info("Initial Super Admin account provisioned");
        }

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "Bootstrap administrator account is inactive"
            );
        }

        // 5. Verify user-role assignment
        boolean alreadyAssigned =
                userPlatformRoleRepository
                        .existsByUser_IdAndRole_Id(
                                user.getId(),
                                savedRole.getId()
                        );

        // Never silently promote an existing ordinary user.
        if (existingUser && !alreadyAssigned) {
            throw new IllegalStateException(
                    "Bootstrap email belongs to an existing " +
                            "account without SYS_SUPER_ADMIN. " +
                            "Use a trusted administrative recovery process."
            );
        }

        // 6. Assign role to newly created bootstrap user
        if (!alreadyAssigned) {

            UserPlatformRoleDao assignment =
                    UserPlatformRoleDao.builder()
                            .user(user)
                            .role(savedRole)
                            .build();

            userPlatformRoleRepository.save(assignment);

            log.info("Initial Super Admin role assigned");
        }

        log.info("Super Admin bootstrap completed");
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
                    "Bootstrap password must contain at least 12 characters"
            );
        }
    }

}
