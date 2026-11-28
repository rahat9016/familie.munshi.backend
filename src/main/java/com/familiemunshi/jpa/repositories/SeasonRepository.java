package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.SeasonDao;
import com.familiemunshi.service.enums.SeasonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SeasonRepository extends JpaRepository<SeasonDao, UUID> {

    String FILTER_WHERE = """
            WHERE LOWER(s.season) LIKE :searchPattern ESCAPE '!'
            AND (:status IS NULL OR s.status = :status)
            """;

    boolean existsBySeasonIgnoreCase(String season);

    boolean existsBySeasonIgnoreCaseAndIdNot(String season, UUID id);

    @Query(
            value = "SELECT s FROM SeasonDao s " + FILTER_WHERE,
            countQuery = "SELECT COUNT(s) FROM SeasonDao s " + FILTER_WHERE
    )
    Page<SeasonDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("status") SeasonStatus status,
            Pageable pageable
    );
}
