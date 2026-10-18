package com.familiemunshi.service;


import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUser(UUID id){
        UserDao user = userRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return UserMapper.toEntity(user);
    }
}