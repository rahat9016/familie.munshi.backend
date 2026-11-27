package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.ColorwayDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ColorwayRepository extends JpaRepository<ColorwayDao, UUID> {

    String FILTER_WHERE = """
            WHERE (
                LOWER(c.code) LIKE :searchPattern ESCAPE '!'
                OR LOWER(c.name) LIKE :searchPattern ESCAPE '!'
                OR LOWER(c.marketingName) LIKE :searchPattern ESCAPE '!'
            )
            AND c.code LIKE :codePattern ESCAPE '!'
            AND (:colorStandard = '' OR c.colorStandard = :colorStandard)
            AND (:filterActive = false OR c.isActive = :activeValue)
            AND (:filterInTheme = false OR c.inTheme = :inThemeValue)
            """;

    boolean existsByCode(String code);

    @Query(
            value = "SELECT c FROM ColorwayDao c " + FILTER_WHERE,
            countQuery = "SELECT COUNT(c) FROM ColorwayDao c " + FILTER_WHERE
    )
    Page<ColorwayDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("codePattern") String codePattern,
            @Param("colorStandard") String colorStandard,
            @Param("filterActive") boolean filterActive,
            @Param("activeValue") boolean activeValue,
            @Param("filterInTheme") boolean filterInTheme,
            @Param("inThemeValue") boolean inThemeValue,
            Pageable pageable
    );
}
