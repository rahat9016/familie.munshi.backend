package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.PermissionDao;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository <PermissionDao, UUID> {
    Optional<PermissionDao> findByName(String name);
    boolean existsByName(String name);
    List<PermissionDao> findAllByNameIn(
           Collection<String> names
    );


    @Query("""
        SELECT p
        FROM PermissionDao p
        WHERE p.name IN :permissionCodes
          AND (
              LOWER(p.name) LIKE :searchPattern ESCAPE '!'
              OR LOWER(p.description) LIKE :searchPattern ESCAPE '!'
          )
        """)
    Page<PermissionDao> findAllByFilters(
            @Param("permissionCodes") Collection<String> permissionCodes,
            @Param("searchPattern") String searchPattern,
            Pageable pageable
    );

}
