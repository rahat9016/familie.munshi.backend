package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.UserPlatformRoleDao;
import com.familiemunshi.service.enums.RoleScope;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPlatformRoleRepository  extends JpaRepository<UserPlatformRoleDao, UUID> {
    boolean existsByUser_IdAndRole_Id(UUID userId, UUID roleId);

    List<UserPlatformRoleDao> findByUser_Id(UUID userId);

    Optional<UserPlatformRoleDao> findByUser_IdAndRole_Id(
            UUID userId,
            UUID roleId
    );


    @Query("""
    SELECT COUNT(upr) > 0
    FROM UserPlatformRoleDao upr
    JOIN upr.role role
    JOIN role.permissions permission
    WHERE upr.user.id = :userId
      AND upr.user.isActive = true
      AND role.isActive = true
      AND role.scope = :scope
      AND permission.name = :permissionCode
    """)
    boolean hasPermission(
            @Param("userId") UUID userId,
            @Param("scope") RoleScope scope,
            @Param("permissionCode") String permissionCode
    );
}
