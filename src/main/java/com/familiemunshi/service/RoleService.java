package com.familiemunshi.service;

import com.familiemunshi.common.policy.PermissionPolicy;
import com.familiemunshi.common.utils.RoleNameUtil;
import com.familiemunshi.common.utils.Util;
import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.jpa.repositories.RoleRepository;
import com.familiemunshi.jpa.repositories.UserBranchRepository;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;
import com.familiemunshi.jpa.repositories.UserPlatformRoleRepository;
import com.familiemunshi.mapper.RoleMapper;
import com.familiemunshi.service.entities.Role;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.RoleAlreadyExistsException;
import com.familiemunshi.service.exceptions.RoleNotFoundException;
import com.familiemunshi.service.filters.FindAllRolesFilter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserPlatformRoleRepository userPlatformRoleRepository;
    private final UserBranchRepository userBranchRepository;
    private final UserBranchRoleRepository userBranchRoleRepository;

    public Role createSystemRole(Role role) {
        Role newRole = role
                .withScope(RoleScope.PLATFORM)
                .withName(RoleNameUtil.platformRole(role.name()))
                .withIsSystem(false)
                .withIsActive(true);
        if(roleRepository.existsByNameIgnoreCaseAndScope(newRole.name(), newRole.scope())){
            throw new RoleAlreadyExistsException(newRole.name());
        }
        // MAP THE ROLE ENTITY TO DAO
        RoleDao roleMapper = RoleMapper.toDao(newRole);

        // SAVE THE ROLE DAO TO THE DATABASE
        RoleDao savedRole = roleRepository.save(roleMapper);

        return RoleMapper.toEntity(savedRole);
    }

    public Role getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .map(RoleMapper::toEntity)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    public Role createBranchRole(UUID userId, UUID branchId, Role role) {
        Role newRole = role
                .withScope(RoleScope.BRANCH)
//                .withName(RoleNameUtil.branchRole(role.name()))
                .withIsSystem(false)
                .withIsActive(true);
        if(roleRepository.existsByNameIgnoreCaseAndScope(newRole.name(), newRole.scope())){
            throw new RoleAlreadyExistsException(newRole.name());
        }
        // MAP THE ROLE ENTITY TO DAO
        RoleDao roleMapper = RoleMapper.toDao(newRole);

        // SAVE THE ROLE DAO TO THE DATABASE
        RoleDao savedRole = roleRepository.save(roleMapper);

        return RoleMapper.toEntity(savedRole);
    }


    public Page<Role> getAllRolesByFilter(
            FindAllRolesFilter filter,
            Pageable pageable
    ) {

        String searchPattern = Util.toSearchPattern(filter.getSearch());

        boolean filterActive = filter.getIsActive() != null;
        boolean activeValue = Boolean.TRUE.equals(filter.getIsActive());

        boolean filterSystem = filter.getIsSystem() != null;
        boolean systemValue = Boolean.TRUE.equals(filter.getIsSystem());

        Page<RoleDao> roles = roleRepository.findAllByFilters(
                RoleScope.PLATFORM,
                searchPattern,
                filterActive,
                activeValue,
                filterSystem,
                systemValue,
                pageable
        );

        return roles.map(RoleMapper::toEntity);
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


    public boolean hasBranchPermission(
            Authentication authentication,
            UUID branchId,
            String permissionCode
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UUID userId;

        try {
            userId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            return false;
        }

        return hasBranchPermission(userId, branchId, permissionCode);
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
                userId, RoleScope.PLATFORM, permissionCode
        )) {
            return true;
        }

        return PermissionPolicy.isBranchPermission(permissionCode)
                && userBranchRepository.existsByUser_IdAndBranch_Id(userId, branchId)
                && userBranchRoleRepository.hasBranchPermission(
                userId, branchId, RoleScope.BRANCH, permissionCode
        );
    }

}
