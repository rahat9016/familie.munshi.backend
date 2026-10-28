package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.UserDao;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserDao, UUID> {
    Optional<UserDao> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserDao> findByEmailAndPassword(
            String email,
            String password
    );
    Optional<UserDao> findByIdAndIsActiveTrue(UUID id);
    boolean existsByIdAndIsActiveTrue(UUID id);

    @Query("""
            SELECT u
            FROM UserDao u
            WHERE (
                LOWER(u.firstName) LIKE :searchPattern ESCAPE '!'
                OR LOWER(u.lastName) LIKE :searchPattern ESCAPE '!'
                OR LOWER(u.email) LIKE :searchPattern ESCAPE '!'
                OR LOWER(u.phone) LIKE :searchPattern ESCAPE '!'
            )
            AND (
                :filterActive = false
                OR u.isActive = :activeValue
            )
            """)
    Page<UserDao> findAllByFilters(
            @Param("searchPattern") String searchPattern,
            @Param("filterActive") boolean filterActive,
            @Param("activeValue") boolean activeValue,
            Pageable pageable
    );
}
