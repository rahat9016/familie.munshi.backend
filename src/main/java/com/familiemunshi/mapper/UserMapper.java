package com.familiemunshi.mapper;

import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.service.entities.User;

public class UserMapper {
    public UserMapper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static User toEntity(UserDao userDao) {
        return User.builder()
                .id(userDao.getId())
                .profilePicture(userDao.getProfilePicture())
                .firstName(userDao.getFirstName())
                .lastName(userDao.getLastName())
                .email(userDao.getEmail())
                .password(userDao.getPassword())
                .isActive(userDao.isActive())
                .createdAt(userDao.getCreatedAt())
                .updatedAt(userDao.getUpdatedAt())
                .build();
    }

    public static UserDao toDao(User user, String salt) {
        return UserDao.builder()
                .id(user.id())
                .profilePicture(user.profilePicture())
                .firstName(user.firstName())
                .lastName(user.lastName())
                .email(user.email())
                .password(user.password())
                .salt(salt)
                .isActive(user.isActive())
                .createdAt(user.createdAt())
                .updatedAt(user.updatedAt())
                .build();
    }
}
