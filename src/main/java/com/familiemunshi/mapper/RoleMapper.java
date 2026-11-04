package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.service.entities.Role;

import java.util.HashSet;
import java.util.Set;

public class RoleMapper {
    public RoleMapper(){
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Role toEntity(RoleDao roleDao) {
        return Role.builder()
                .id(roleDao.getId())
                .description(roleDao.getDescription())
                .name(roleDao.getName())
                .scope(roleDao.getScope())
                .isSystem(roleDao.getIsSystem())
                .isActive(roleDao.getIsActive())
                .createdAt(roleDao.getCreatedAt())
                .updatedAt(roleDao.getUpdatedAt())
                .build();
    }

    public static RoleDao toDao(Role role){
        return RoleDao.builder()
                .id(role.id())
                .name(role.name())
                .description(role.description())
                .scope(role.scope())
                .isSystem(role.isSystem())
                .isActive(role.isActive())
                .createdAt(role.createdAt())
                .updatedAt(role.updatedAt())
                .build();
    }
    public static RoleDao toDao(
            Role role,
            BranchDao branch,
            Set<PermissionDao> permissions
    ) {

        RoleDao roleDao = toDao(role);

        if (roleDao == null) {
            return null;
        }

        roleDao.setBranch(branch);

        roleDao.setPermissions(
                permissions == null
                        ? new HashSet<>()
                        : new HashSet<>(permissions)
        );

        return roleDao;
    }
}
