package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.service.enums.RoleScope;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository <RoleDao, UUID >, JpaSpecificationExecutor<RoleDao> {
    Optional <RoleDao> findByName(String name);
    boolean existsByNameIgnoreCaseAndScope(String name, RoleScope scope);

    Optional<RoleDao> findByNameIgnoreCaseAndScope(
            String name,
            RoleScope scope
    );

    @Query("""
            SELECT r
            FROM RoleDao r
            WHERE r.scope = :scope
              AND (
                  LOWER(r.name) LIKE :searchPattern ESCAPE '!'
              )
              AND (
                  :filterActive = false
                  OR r.isActive = :activeValue
              )
              AND (
                  :filterSystem = false
                  OR r.isSystem = :systemValue
              )
            """)
    Page<RoleDao> findAllByFilters(
            @Param("scope") RoleScope scope,
            @Param("searchPattern") String searchPattern,
            @Param("filterActive") boolean filterActive,
            @Param("activeValue") boolean activeValue,
            @Param("filterSystem") boolean filterSystem,
            @Param("systemValue") boolean systemValue,
            Pageable pageable
    );
}
