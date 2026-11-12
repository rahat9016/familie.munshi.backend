package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.BranchDao;
import com.familiemunshi.jpa.daos.UserBranchRoleDao;
import com.familiemunshi.service.enums.RoleScope;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserBranchRoleRepository
        extends JpaRepository<UserBranchRoleDao, UUID> {

    @Query("""
                SELECT DISTINCT permission.name
                FROM UserBranchRoleDao ubr
                JOIN ubr.role role
                JOIN role.permissions permission
                WHERE ubr.user.id = :userId
                  AND ubr.branch.id = :branchId
                  AND ubr.user.isActive = true
                  AND ubr.branch.isActive = true
                  AND role.isActive = true
                  AND role.scope = :scope
                  AND role.branch.id = :branchId
            """)
    List<String> findEffectivePermissionCodes(
            @Param("userId") UUID userId,
            @Param("branchId") UUID branchId,
            @Param("scope") RoleScope scope
    );

    @Query("""
                SELECT COUNT(ubr) > 0
                FROM UserBranchRoleDao ubr
                JOIN ubr.role role
                JOIN role.permissions permission
                WHERE ubr.user.id = :userId
                  AND ubr.branch.id = :branchId
                  AND ubr.user.isActive = true
                  AND ubr.branch.isActive = true
                  AND role.isActive = true
                  AND role.scope = :scope
                  AND role.branch.id = :branchId
                  AND permission.name = :permissionCode
            """)
    boolean hasBranchPermission(
            @Param("userId") UUID userId,
            @Param("branchId") UUID branchId,
            @Param("scope") RoleScope scope,
            @Param("permissionCode") String permissionCode
    );

    @Query("""
                SELECT DISTINCT role.name
                FROM UserBranchRoleDao ubr
                JOIN ubr.role role
                WHERE ubr.user.id = :userId
                  AND ubr.branch.id = :branchId
                  AND ubr.user.isActive = true
                  AND ubr.branch.isActive = true
                  AND role.isActive = true
                  AND role.scope = :scope
                  AND role.branch.id = :branchId
            """)
    List<String> findRoleNames(
            @Param("userId") UUID userId,
            @Param("branchId") UUID branchId,
            @Param("scope") RoleScope scope
    );


    @Query("""
                SELECT COUNT(ubr) > 0
                FROM UserBranchRoleDao ubr
                JOIN ubr.role role
                WHERE ubr.user.id = :userId
                  AND ubr.user.isActive = false
                  AND ubr.branch.isActive = true
                  AND role.name = :roleName
                  AND role.scope = :scope
                  AND role.isActive = true
                  AND role.branch.id = ubr.branch.id
            """)
    boolean hasPendingBranchAdminAssignment(
            @Param("userId") UUID userId,
            @Param("roleName") String roleName,
            @Param("scope") RoleScope scope
    );


    // GET USER'S AUTHORIZED ACTIVE BRANCHES
    @Query("""
                SELECT DISTINCT branch
                FROM UserBranchRoleDao ubr
                JOIN ubr.branch branch
                JOIN ubr.role role
                WHERE ubr.user.id = :userId
                  AND ubr.user.isActive = true
                  AND branch.isActive = true
                  AND role.isActive = true
                  AND role.scope = :scope
                  AND role.branch.id = branch.id
                ORDER BY branch.name
            """)
    List<BranchDao> findAuthorizedBranches(@Param("userId") UUID userId, @Param("scope") RoleScope scope);
}