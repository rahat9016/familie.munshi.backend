package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.UserPlatformRoleDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPlatformRoleRepository  extends JpaRepository<UserPlatformRoleDao, UUID> {
    boolean existsByUser_IdAndRole_Id(UUID userId, UUID roleId);

    List<UserPlatformRoleDao> findByUser_Id(UUID userId);

    Optional<UserPlatformRoleDao> findByUser_IdAndRole_Id(
            UUID userId,
            UUID roleId
    );
}
