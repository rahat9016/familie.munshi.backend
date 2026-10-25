package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.RoleDao;
import com.familiemunshi.service.enums.RoleScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository <RoleDao, UUID > {
    Optional <RoleDao> findByName(String name);
    boolean existsByNameIgnoreCaseAndScope(String name, RoleScope scope);
}
