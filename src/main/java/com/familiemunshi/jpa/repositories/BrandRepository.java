package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.BrandDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BrandRepository extends JpaRepository<BrandDao, UUID> {

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, UUID id);

    boolean existsByPosition(Integer position);

    boolean existsByPositionAndIdNot(Integer position, UUID id);

    java.util.List<BrandDao> findAllByIsActiveTrueOrderByPositionAsc();

    @Query(
            value = """
                    SELECT b
                    FROM BrandDao b
                    WHERE LOWER(b.title) LIKE :searchPattern ESCAPE '!'
                    AND (
                        :filterActive = false
                        OR b.isActive = :activeValue
                    )
                    """,
            countQuery = """
                    SELECT COUNT(b)
                    FROM BrandDao b
                    WHERE LOWER(b.title) LIKE :searchPattern ESCAPE '!'
                    AND (
                        :filterActive = false
                        OR b.isActive = :activeValue
                    )
                    """
    )
    Page<BrandDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("filterActive") boolean filterActive,
            @Param("activeValue") boolean activeValue,
            Pageable pageable
    );
}
