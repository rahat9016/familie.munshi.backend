package com.familiemunshi.service;


import com.familiemunshi.common.service.FileStorageService;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.*;
import com.familiemunshi.jpa.repositories.*;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.*;
import com.familiemunshi.service.filters.FindAllUsersFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionAuthorizationService permissionAuthorizationService;
    private final FileStorageService fileStorageService;
    private final UserPlatformRoleRepository userPlatformRoleRepository;
    private final BranchRepository branchRepository;
    private final UserBranchRoleRepository userBranchRoleRepository;
    private final RoleService roleService;

    @Transactional
    public User createUserWithRole(
            UUID callerId,
            User user,
            UUID roleId,
            UUID branchId,
            MultipartFile profilePicture
    ) {

        // 1. CHECK USER CREATE PERMISSION
        permissionAuthorizationService.requirePlatformPermission(
                callerId,
                "USER_CREATE"
        );

        // 2. NORMALIZE EMAIL
        String email = user.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        // 3. CHECK DUPLICATE EMAIL
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserEmailAlreadyExistsException(email);
        }

        // 4. FIND ROLE
        RoleDao role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(roleId)
                );

        if (!Boolean.TRUE.equals(role.getIsActive())) {
            throw new PermissionDeniedException("Inactive role cannot be assigned");
        }

        if (role.getIsSystem() && "SYS_SUPER_ADMIN".equals(role.getName())) {
            throw new InvalidUserCreationException(
                    "SYS_SUPER_ADMIN role cannot be assigned during user creation"
            );
        }

        // 5. VALIDATE ROLE SCOPE
        if (role.getScope() == RoleScope.PLATFORM) {

            if (branchId != null || role.getBranch() != null) {
                throw new IllegalArgumentException(
                        "Platform role cannot have a branch"
                );
            }

        } else if (role.getScope() == RoleScope.BRANCH) {

            if (branchId == null) {
                throw new IllegalArgumentException(
                        "Branch ID is required for branch role"
                );
            }

            if (role.getBranch() == null
                    || !role.getBranch().getId().equals(branchId)) {
                throw new IllegalArgumentException(
                        "Role does not belong to the selected branch"
                );
            }

        } else {
            throw new IllegalArgumentException(
                    "Unsupported role scope"
            );
        }

        // 6. CHECK CALLER'S ROLE ASSIGNMENT AUTHORITY
        if (role.getScope() == RoleScope.PLATFORM) {

            permissionAuthorizationService.requirePlatformPermission(
                    callerId,
                    "USER_ROLE_ASSIGN"
            );

            requireRoleWithinCallerAccess(callerId, role);

        } else {

            permissionAuthorizationService.requireBranchPermission(
                    callerId,
                    branchId,
                    "USER_ROLE_ASSIGN"
            );
        }

        // 7. GENERATE PASSWORD HASH
        String salt = Util.generateSalt();

        String hashedPassword = Util.hashPassword(
                user.password(),
                salt
        );

        User preparedUser = user
                .withEmail(email)
                .withPassword(hashedPassword);

        // 8. OPTIONAL PROFILE PICTURE
        String profilePictureKey = null;

        if (profilePicture != null && !profilePicture.isEmpty()) {

            ValidationUtil.validateMediaType(profilePicture);

            String filename = fileStorageService.uploadPublicFile(
                    profilePicture,
                    "users/profile-pictures"
            );

            registerUploadedFileCleanup(filename);

            profilePictureKey = filename;

            preparedUser = preparedUser.withProfilePicture(filename);
        }

        // 9. MAP USER ENTITY TO DAO
        UserDao userDao = UserMapper.toDao(
                preparedUser,
                salt
        );

        // 10. SAVE USER
        UserDao savedUser = userRepository.saveAndFlush(userDao);

        // 11. ASSIGN ROLE
        if (role.getScope() == RoleScope.PLATFORM) {

            UserPlatformRoleDao assignment =
                    UserPlatformRoleDao.builder()
                            .user(savedUser)
                            .role(role)
                            .build();

            userPlatformRoleRepository.saveAndFlush(assignment);

        } else {

            BranchDao branch = branchRepository.findById(branchId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Branch not found: " + branchId
                            )
                    );

            if (!branch.isActive()) {
                throw new PermissionDeniedException(
                        "Cannot assign a role to an inactive branch"
                );
            }

            UserBranchRoleDao assignment =
                    UserBranchRoleDao.builder()
                            .user(savedUser)
                            .branch(branch)
                            .role(role)
                            .build();

            userBranchRoleRepository.saveAndFlush(assignment);
        }

        // 12. RETURN RESPONSE
        return UserMapper.toEntity(savedUser);
    }

    // This method is used to get a user by ID, but it only returns the user if they are active.
    @Cacheable(value = "users", key = "'active:' + #id")
    @Transactional(readOnly = true)
    public User getUser(UUID id){
        UserDao user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return UserMapper.toEntity(user);
    }

    // This method is used to get a user by ID without checking if the user is active or not.
    // It is useful for admin purposes where you might want to retrieve users regardless of their active status.
    @Cacheable(value = "users", key = "'all:' + #id")
    @Transactional(readOnly = true)
    public User getUserById(UUID id){
        UserDao user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return UserMapper.toEntity(user);
    }



    @Transactional(readOnly = true)
    public Page<User> getAllUsersByFilter(
            FindAllUsersFilter filter,
            Pageable pageable
    ) {

        String searchPattern = Util.toSearchPattern(
                filter.getSearch()
        );

        boolean filterActive = filter.getIsActive() != null;

        boolean activeValue = Boolean.TRUE.equals(
                filter.getIsActive()
        );

        Page<UserDao> users = userRepository.findAllByFilters(
                searchPattern,
                filterActive,
                activeValue,
                pageable
        );

        return users.map(UserMapper::toEntity);
    }


    private void requireRoleWithinCallerAccess(
            UUID callerId,
            RoleDao role
    ) {

        Set<String> callerPermissions = new HashSet<>(
                userPlatformRoleRepository.findEffectivePermissionCodes(
                        callerId,
                        RoleScope.PLATFORM
                )
        );

        Set<String> requestedPermissions = role.getPermissions()
                .stream()
                .map(PermissionDao::getName)
                .collect(Collectors.toSet());

        if (!callerPermissions.containsAll(requestedPermissions)) {
            throw new PermissionDeniedException(
                    "You cannot assign a role beyond your own permissions"
            );
        }
    }

    private void registerUploadedFileCleanup(String filename) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status == STATUS_COMMITTED) {
                            return;
                        }

                        try {
                            fileStorageService.deleteFile(filename);
                        } catch (Exception exception) {
                            log.error(
                                    "Failed to cleanup profile picture: {}",
                                    filename,
                                    exception
                            );
                        }
                    }
                }
        );
    }
}