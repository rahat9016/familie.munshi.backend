
package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.ProductClassificationDao;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductClassificationRepository
        extends JpaRepository<ProductClassificationDao, UUID> {

    Optional<ProductClassificationDao> findByIdAndIsActiveTrue(
            UUID id
    );

    @Query(
            value = """
                    SELECT c
                    FROM ProductClassificationDao c
                    LEFT JOIN FETCH c.parent p
                    WHERE (
                        LOWER(c.name) LIKE :searchPattern ESCAPE '!'
                        OR LOWER(c.code) LIKE :searchPattern ESCAPE '!'
                    )
                    AND (
                        :filterLevel = false
                        OR c.level = :level
                    )
                    AND (
                        :filterActive = false
                        OR c.isActive = :activeValue
                    )
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM ProductClassificationDao c
                    WHERE (
                        LOWER(c.name) LIKE :searchPattern ESCAPE '!'
                        OR LOWER(c.code) LIKE :searchPattern ESCAPE '!'
                    )
                    AND (
                        :filterLevel = false
                        OR c.level = :level
                    )
                    AND (
                        :filterActive = false
                        OR c.isActive = :activeValue
                    )
                    """
    )
    Page<ProductClassificationDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("filterLevel") boolean filterLevel,
            @Param("level") ProductClassificationLevel level,
            @Param("filterActive") boolean filterActive,
            @Param("activeValue") boolean activeValue,
            Pageable pageable
    );

    List<ProductClassificationDao>
    findAllByParent_IdAndIsActiveTrueOrderByNameAsc(UUID parentId);

    List<ProductClassificationDao>
    findAllByParentIsNullAndIsActiveTrueOrderByNameAsc();

    List<ProductClassificationDao>
    findAllByLevelAndIsActiveTrueOrderByNameAsc(
            ProductClassificationLevel level
    );

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            UUID id
    );

    boolean existsByParent_IdAndIsActiveTrue(UUID parentId);

    /*
     * The given classification and all of its active descendants.
     * An inactive row stops the walk, so its whole branch is excluded.
     */
    @Query(
            value = """
                    WITH RECURSIVE subtree AS (
                        SELECT *
                        FROM product_classifications
                        WHERE id = :rootId
                          AND is_active = true

                        UNION ALL

                        SELECT c.*
                        FROM product_classifications c
                        JOIN subtree s ON c.parent_id = s.id
                        WHERE c.is_active = true
                    )
                    SELECT *
                    FROM subtree
                    ORDER BY name
                    """,
            nativeQuery = true
    )
    List<ProductClassificationDao> findActiveSubtree(
            @Param("rootId") UUID rootId
    );
}
