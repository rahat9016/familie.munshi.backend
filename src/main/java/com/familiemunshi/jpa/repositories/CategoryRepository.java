package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.CategoryDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryDao, UUID> {

    String FILTER_WHERE = """
            WHERE LOWER(c.name) LIKE :searchPattern ESCAPE '!'
            AND (:departmentId IS NULL OR c.departmentId = :departmentId)
            AND (:isActive IS NULL OR c.isActive = :isActive)
            """;

    boolean existsByDepartmentIdAndNameIgnoreCase(UUID departmentId, String name);

    boolean existsByDepartmentIdAndNameIgnoreCaseAndIdNot(UUID departmentId, String name, UUID id);

    boolean existsByDepartmentId(UUID departmentId);

    List<CategoryDao> findAllByDepartmentIdInOrderByNameAsc(Collection<UUID> departmentIds);

    @Query(
            value = "SELECT c FROM CategoryDao c " + FILTER_WHERE,
            countQuery = "SELECT COUNT(c) FROM CategoryDao c " + FILTER_WHERE
    )
    Page<CategoryDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("departmentId") UUID departmentId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
