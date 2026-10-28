package com.familiemunshi.service;


import com.familiemunshi.common.utils.Util;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.exceptions.UserNotFoundException;
import com.familiemunshi.service.filters.FindAllUsersFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // This method is used to get a user by ID, but it only returns the user if they are active.
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUser(UUID id){
        UserDao user = userRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return UserMapper.toEntity(user);
    }

    // This method is used to get a user by ID without checking if the user is active or not.
    // It is useful for admin purposes where you might want to retrieve users regardless of their active status.
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUserById(UUID id){
        UserDao user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return UserMapper.toEntity(user);
    }



    @Transactional(readOnly = true)
    public Page<User> getAllUsersByFilter(
            FindAllUsersFilter filter,
            Pageable pageable
    ) {

        String searchPattern = Util.toSearchPattern(
                filter.getSearch()
        );

        boolean filterActive = filter.getIsActive() != null;

        boolean activeValue = Boolean.TRUE.equals(
                filter.getIsActive()
        );

        Page<UserDao> users = userRepository.findAllByFilters(
                searchPattern,
                filterActive,
                activeValue,
                pageable
        );

        return users.map(UserMapper::toEntity);
    }

}