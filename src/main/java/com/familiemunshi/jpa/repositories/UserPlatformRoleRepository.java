package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.UserPlatformRoleDao;
import com.familiemunshi.service.enums.RoleScope;
import org.springframework.data.repository.query.Param;
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
              AND role.branch IS NULL
              AND permission.name = :permissionCode
            """)
    boolean hasPermission(
            @Param("userId") UUID userId,
            @Param("scope") RoleScope scope,
            @Param("permissionCode") String permissionCode
    );


    @Query("""
                SELECT DISTINCT role.name
                FROM UserPlatformRoleDao upr
                JOIN upr.role role
                WHERE upr.user.id = :userId
                  AND upr.user.isActive = true
                  AND role.isActive = true
                  AND role.scope = :scope
                  AND role.branch IS NULL
                ORDER BY role.name
            """)
    List<String> findEffectiveRoleNames(@Param("userId") UUID userId, @Param("scope") RoleScope scope);

    @Query("""
                SELECT DISTINCT permission.name
                FROM UserPlatformRoleDao upr
                JOIN upr.role role
                JOIN role.permissions permission
                WHERE upr.user.id = :userId
                  AND upr.user.isActive = true
                  AND role.isActive = true
                  AND role.scope = :scope
                  AND role.branch IS NULL
                ORDER BY permission.name
            """)
    List<String> findEffectivePermissionCodes(@Param("userId") UUID userId, @Param("scope") RoleScope scope);



}
