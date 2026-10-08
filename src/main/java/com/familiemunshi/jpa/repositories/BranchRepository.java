package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.BranchDao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BranchRepository extends  JpaRepository<BranchDao, Long> {
    Optional<BranchDao> findByCode(String code);

    boolean existsByCode(String code);
    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String name, Long id);

}
