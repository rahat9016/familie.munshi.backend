
package com.familiemunshi.service;


import com.familiemunshi.common.policy.PermissionPolicy;
import com.familiemunshi.common.utils.BranchAdminGenerator;
import com.familiemunshi.common.utils.Util;

import com.familiemunshi.http.validations.ValidationUtil;
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

import com.familiemunshi.mapper.RoleMapper;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.BranchAdminCredentials;
import com.familiemunshi.service.entities.Role;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.UserEmailAlreadyExistsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
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

    @Transactional(propagation = Propagation.MANDATORY)
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

        ValidationUtil.validateBranchAdminPassword(
                commonPassword
        );

        // ======================================
        // 4. HASH COMMON PASSWORD
        // ======================================

        String salt = Util.generateSalt();

        String hashedPassword = Util.hashPassword(
                commonPassword,
                salt
        );

        // ======================================
        // 5. CREATE FIRST ADMIN USER
        // ======================================

        String firstName = branch.getName().trim();

        if (firstName.length() > 120) {
            firstName = firstName.substring(0, 120);
        }

        User adminUser = User.builder()
                .firstName(firstName)
                .lastName("Admin")
                .email(email)
                .password(hashedPassword)
                .isActive(false)
                .build();

        UserDao adminDao = UserMapper.toDao(
                adminUser,
                salt
        );

        UserDao savedAdmin = userRepository.saveAndFlush(adminDao);

        // ======================================
        // 6. CREATE BRANCH_ADMIN ROLE
        // ======================================

        Role adminRole = Role.builder()
                .name(BRANCH_ADMIN_ROLE)
                .description(
                        "Administrator of Branch "
                                + branch.getCode()
                )
                .scope(RoleScope.BRANCH)
                .isSystem(true)
                .isActive(true)
                .build();

        RoleDao roleDao = RoleMapper.toDao(
                adminRole,
                branch,
                new HashSet<>(permissions)
        );

        RoleDao savedRole =
                roleRepository.saveAndFlush(roleDao);

        // ======================================
        // 7. CREATE USER-BRANCH MEMBERSHIP
        // ======================================

        UserBranchDao membership =
                UserBranchDao.builder()
                        .user(savedAdmin)
                        .branch(branch)
                        .build();

        userBranchRepository.saveAndFlush(membership);

        // ======================================
        // 8. ASSIGN ADMIN ROLE TO USER
        // ======================================

        UserBranchRoleDao assignment =
                UserBranchRoleDao.builder()
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
