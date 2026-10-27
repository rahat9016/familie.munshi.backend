package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.service.enums.RoleScope;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository <RoleDao, UUID > {
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
              :search IS NULL
              OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(r.description, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (
              :isActive IS NULL
              OR r.isActive = :isActive
          )
          AND (
              :isSystem IS NULL
              OR r.isSystem = :isSystem
          )
        """)
    Page<RoleDao> findAllByFilters(
            @Param("scope") RoleScope scope,
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("isSystem") Boolean isSystem,
            Pageable pageable
    );

}
