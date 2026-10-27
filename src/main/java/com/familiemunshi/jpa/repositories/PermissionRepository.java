package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.PermissionDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository <PermissionDao, UUID> {
    Optional<PermissionDao> findByName(String name);
    boolean existsByName(String name);
    List<PermissionDao> findAllByNameIn(
           Collection<String> names
    );
}
