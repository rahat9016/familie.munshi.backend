package com.familiemunshi.service;

import com.familiemunshi.common.policy.PermissionPolicy;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.responses.BranchPermissionResponse;
import com.familiemunshi.http.dtos.responses.MyPermissionsResponse;
import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.mapper.PermissionMapper;
import com.familiemunshi.service.entities.Permission;
import com.familiemunshi.service.entities.PermissionGroup;
import com.familiemunshi.service.enums.PermissionCode;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.PermissionNotFoundException;
import com.familiemunshi.service.exceptions.UserNotFoundException;
import com.familiemunshi.service.filters.FindAllPermissionsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final UserRepository userRepository;
    private final UserPlatformRoleRepository userPlatformRoleRepository;
    private final UserBranchRoleRepository userBranchRoleRepository;
    private final PermissionRepository permissionRepository;

    // ==========================================
    // 1. GET ALL PERMISSIONS
    // ==========================================
    @Transactional(readOnly = true)
    public Page<Permission> getAllPermissionsByFilter(
            FindAllPermissionsFilter filter,
            Pageable pageable
    ) {

        String searchPattern = Util.toSearchPattern(
                filter.getSearch()
        );

        String module = filter.getModule() == null
                ? null
                : filter.getModule()
                .trim()
                .toUpperCase(Locale.ROOT);

        List<String> permissionCodes =
                Arrays.stream(PermissionCode.values())
                        .filter(permission ->
                                module == null
                                        || module.isBlank()
                                        || permission.getModule().equals(module)
                        )
                        .map(PermissionCode::getCode)
                        .toList();

        if (permissionCodes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid permission module: " + module
            );
        }

        Page<PermissionDao> permissions =
                permissionRepository.findAllByFilters(
                        permissionCodes,
                        searchPattern,
                        pageable
                );

        return permissions.map(
                PermissionMapper::toEntity
        );
    }

    // ==========================================
    // 2. GET PERMISSION TREE
    // ==========================================
    @Transactional(readOnly = true)
    public List<PermissionGroup> getPermissionTree() {

        List<Permission> permissions =
                permissionRepository.findAll()
                        .stream()
                        .map(PermissionMapper::toEntity)
                        .toList();

        Map<String, List<Permission>> groupedPermissions =
                permissions.stream()
                        .collect(Collectors.groupingBy(
                                permission ->
                                        permission.code().getModule(),
                                TreeMap::new,
                                Collectors.toList()
                        ));

        return groupedPermissions.entrySet()
                .stream()
                .map(entry -> new PermissionGroup(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .sorted(Comparator.comparing(
                                        permission ->
                                                permission.code().getCode()
                                ))
                                .toList()
                ))
                .toList();
    }

    // ==========================================
    // 3. GET PERMISSION BY ID
    // ==========================================
    @Transactional(readOnly = true)
    public Permission getPermissionById(UUID id) {
        PermissionDao permission =
                permissionRepository.findById(id)
                        .orElseThrow(() ->
                                new PermissionNotFoundException(
                                        id.toString()
                                )
                        );

        return PermissionMapper.toEntity(permission);
    }

    // ==========================================
    // 4. GET MY EFFECTIVE PERMISSIONS
    // ==========================================

    @Transactional(readOnly = true)
    public MyPermissionsResponse getMyPermissions(
            UUID userId
    ) {

        // 4.1 VERIFY ACTIVE USER
        if (!userRepository.existsByIdAndIsActiveTrue(userId)) {
            throw new UserNotFoundException(
                    userId.toString()
            );
        }
        // ==========================================
        // 4.2 CHECK PLATFORM ROLE ASSIGNMENT
        // ==========================================

        List<String> platformRoles =
                userPlatformRoleRepository
                        .findEffectiveRoleNames(
                                userId,
                                RoleScope.PLATFORM
                        );

        // PLATFORM USER
        if (!platformRoles.isEmpty()) {

            List<String> platformPermissions =
                    userPlatformRoleRepository
                            .findEffectivePermissionCodes(
                                    userId,
                                    RoleScope.PLATFORM
                            )
                            .stream()
                            .filter(
                                    PermissionPolicy::isPlatformPermission
                            )
                            .distinct()
                            .sorted()
                            .toList();

            return MyPermissionsResponse.platform(
                    userId,
                    platformRoles,
                    platformPermissions
            );
        }

        // ==========================================
        // 4.3 CHECK BRANCH ROLE ASSIGNMENTS
        // ==========================================

        List<BranchDao> branches =
                userBranchRoleRepository
                        .findAuthorizedBranches(
                                userId,
                                RoleScope.BRANCH
                        );

        if (branches.isEmpty()) {
            throw new AccessDeniedException(
                    "No active role assignment found"
            );
        }

        // ==========================================
        // 4.4 LOAD EACH BRANCH'S PERMISSIONS
        // ==========================================

        List<BranchPermissionResponse> branchPermissions =
                branches.stream()
                        .map(branch ->
                                getBranchPermissions(
                                        userId,
                                        branch
                                )
                        )
                        .toList();

        // ==========================================
        // 4.5 SINGLE BRANCH USER
        // ==========================================

        if (branchPermissions.size() == 1) {
            return MyPermissionsResponse.branch(
                    userId,
                    branchPermissions.get(0)
            );
        }

        // ==========================================
        // 4.6 MULTIPLE BRANCH USER
        // ==========================================

        return MyPermissionsResponse.multipleBranches(
                userId,
                branchPermissions
        );
    }

    // ==========================================
    // 5. GET SPECIFIC BRANCH'S PERMISSIONS
    // ==========================================

    private BranchPermissionResponse getBranchPermissions(
            UUID userId,
            BranchDao branch
    ) {

        UUID branchId = branch.getId();

        // GET ASSIGNED ROLE NAMES

        List<String> roles =
                userBranchRoleRepository
                        .findRoleNames(
                                userId,
                                branchId,
                                RoleScope.BRANCH
                        )
                        .stream()
                        .distinct()
                        .sorted()
                        .toList();

        // GET ACTUAL ASSIGNED PERMISSIONS

        List<String> permissions =
                userBranchRoleRepository
                        .findEffectivePermissionCodes(
                                userId,
                                branchId,
                                RoleScope.BRANCH
                        )
                        .stream()
                        .filter(
                                PermissionPolicy::isBranchPermission
                        )
                        .distinct()
                        .sorted()
                        .toList();

        return new BranchPermissionResponse(
                branchId,
                branch.getCode(),
                branch.getName(),
                roles,
                permissions
        );
    }
}
