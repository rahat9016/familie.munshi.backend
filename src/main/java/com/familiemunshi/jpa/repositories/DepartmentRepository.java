package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.DepartmentDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<DepartmentDao, UUID> {

    String FILTER_WHERE = """
            WHERE LOWER(d.name) LIKE :searchPattern ESCAPE '!'
            AND (:isActive IS NULL OR d.isActive = :isActive)
            """;

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query(
            value = "SELECT d FROM DepartmentDao d " + FILTER_WHERE,
            countQuery = "SELECT COUNT(d) FROM DepartmentDao d " + FILTER_WHERE
    )
    Page<DepartmentDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
