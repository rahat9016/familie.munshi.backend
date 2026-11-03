package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.BranchDao;
import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends  JpaRepository<BranchDao, Long> {
    Optional<BranchDao> findByCode(String code);
    Optional<BranchDao> findById(UUID id);
    boolean existsByCode(String code);
    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByNameAndIdNot(String name, UUID id);



    @Query("""
        SELECT b
        FROM BranchDao b
        WHERE (
            LOWER(b.name) LIKE :searchPattern ESCAPE '!'
            OR LOWER(b.code) LIKE :searchPattern ESCAPE '!'
            OR LOWER(b.address) LIKE :searchPattern ESCAPE '!'
            OR LOWER(b.phone) LIKE :searchPattern ESCAPE '!'
        )
        AND (
            :filterActive = false
            OR b.isActive = :activeValue
        )
        """)
    Page<BranchDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("filterActive") boolean filterActive,
            @Param("activeValue") boolean activeValue,
            Pageable pageable
    );

}
