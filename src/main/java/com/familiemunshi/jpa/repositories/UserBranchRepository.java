package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.UserBranchDao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserBranchRepository
        extends JpaRepository<UserBranchDao, UUID> {

    boolean existsByUser_IdAndBranch_Id(
            UUID userId,
            UUID branchId
    );

    @EntityGraph(attributePaths = {"branch"})
    List<UserBranchDao> findByUser_Id(UUID userId);
}

