
package com.familiemunshi.service;


import com.familiemunshi.common.policy.PermissionPolicy;
import com.familiemunshi.common.utils.BranchAdminGenerator;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.common.utils.UuidV7Generator;

import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.daos.UserBranchDao;
import com.familiemunshi.jpa.daos.UserBranchRoleDao;

import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.jpa.repositories.RoleRepository;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.jpa.repositories.UserBranchRepository;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;

import com.familiemunshi.service.entities.BranchAdminCredentials;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.UserEmailAlreadyExistsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BranchAdminProvisioner {

    private static final String BRANCH_ADMIN_ROLE =
            "BRANCH_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserBranchRepository userBranchRepository;
    private final UserBranchRoleRepository userBranchRoleRepository;

    @Value("${branch-admin.email-domain}")
    private String emailDomain;

    @Value("${branch-admin.common-password}")
    private String commonPassword;

    public BranchAdminCredentials provision(BranchDao branch) {

        // ======================================
        // 1. LOAD DEFAULT BRANCH PERMISSIONS
        // ======================================

        List<String> permissionCodes =
                PermissionPolicy.getBranchPermissions();

        List<PermissionDao> permissions =
                permissionRepository.findAllByNameIn(
                        permissionCodes
                );

        Set<String> foundCodes = permissions.stream()
                .map(PermissionDao::getName)
                .collect(Collectors.toSet());

        if (!foundCodes.containsAll(permissionCodes)) {
            throw new IllegalStateException(
                    "Default Branch permissions are not fully seeded"
            );
        }

        // ======================================
        // 2. GENERATE ADMIN EMAIL
        // ======================================

        String email = BranchAdminGenerator.generateEmail(
                branch.getName(),
                branch.getCode(),
                emailDomain
        );

        if (userRepository.existsByEmail(email)) {
            throw new UserEmailAlreadyExistsException(email);
        }

        // ======================================
        // 3. VALIDATE COMMON PASSWORD
        // ======================================

        if (commonPassword == null
                || commonPassword.length() < 16
                || commonPassword.isBlank()) {

            throw new IllegalStateException(
                    "Branch Admin common password must "
                            + "contain at least 16 characters"
            );
        }

        // ======================================
        // 4. HASH COMMON PASSWORD
        // ======================================

        String salt = Util.generateSalt();

        String hashedPassword = Util.hashPassword(
                commonPassword,
                salt
        );

        // Never log or return commonPassword.

        // ======================================
        // 5. CREATE FIRST ADMIN USER
        // ======================================

        String firstName = branch.getName().trim();

        if (firstName.length() > 120) {
            firstName = firstName.substring(0, 120);
        }

        UserDao admin = UserDao.builder()
                .id(UuidV7Generator.generate())
                .firstName(firstName)
                .lastName("Admin")
                .email(email)
                .phone(branch.getPhone())
                .password(hashedPassword)
                .salt(salt)
                .isActive(false)
                .build();

        UserDao savedAdmin =
                userRepository.saveAndFlush(admin);

        // ======================================
        // 6. CREATE BRANCH_ADMIN ROLE
        // ======================================

        RoleDao role = RoleDao.builder()
                .id(UuidV7Generator.generate())
                .name(BRANCH_ADMIN_ROLE)
                .description(
                        "Administrator of Branch "
                                + branch.getCode()
                )
                .scope(RoleScope.BRANCH)
                .branch(branch)
                .isSystem(true)
                .isActive(true)

                // ONLY BRANCH PERMISSIONS
                .permissions(new HashSet<>(permissions))
                .build();

        RoleDao savedRole =
                roleRepository.saveAndFlush(role);

        // ======================================
        // 7. CREATE USER-BRANCH MEMBERSHIP
        // ======================================

        UserBranchDao membership =
                UserBranchDao.builder()
                        .id(UuidV7Generator.generate())
                        .user(savedAdmin)
                        .branch(branch)
                        .build();

        userBranchRepository.saveAndFlush(membership);

        // ======================================
        // 8. ASSIGN ADMIN ROLE TO USER
        // ======================================

        UserBranchRoleDao assignment =
                UserBranchRoleDao.builder()
                        .id(UuidV7Generator.generate())
                        .user(savedAdmin)
                        .branch(branch)
                        .role(savedRole)
                        .build();

        userBranchRoleRepository.saveAndFlush(assignment);

        log.info(
                "Branch Admin provisioned. branchId={}, userId={}",
                branch.getId(),
                savedAdmin.getId()
        );

        // ======================================
        // 9. RETURN NON-SENSITIVE INFORMATION
        // ======================================

        return new BranchAdminCredentials(
                savedAdmin.getId(),
                savedRole.getId(),
                savedAdmin.getEmail(),
                savedRole.getName(),
                savedAdmin.isActive()
        );
    }
}
