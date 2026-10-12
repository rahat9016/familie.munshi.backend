package com.familiemunshi.jpa.repositories;

import com.familiemunshi.jpa.daos.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserDao, UUID> {
    Optional<UserDao> findByEmail(String email);
    boolean existsByEmail(String email);
}
