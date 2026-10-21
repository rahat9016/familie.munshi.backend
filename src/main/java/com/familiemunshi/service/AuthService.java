package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.exceptions.InvalidCredentialsException;
import com.familiemunshi.service.exceptions.UserEmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;

    // SIGNUP
    public User signup(User user) {

        String normalizedEmail = normalizeEmail(user.email());

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserEmailAlreadyExistsException(normalizedEmail);
        }

        // Hash the password with a salt
        String salt = Util.generateSalt();
        String hashedPassword = Util.hashPassword(user.password(), salt);
        // Create a new User object with the hashed password
        User hashedUser = user
                .withEmail(normalizedEmail)
                .withPassword(hashedPassword);

        UserDao userDao = UserMapper.toDao(hashedUser, salt);

        // Save the user to the database
        UserDao savedUser = userRepository.save(userDao);

        // Return the saved user as a User entity
        return UserMapper.toEntity(savedUser);
    }

    // LOGIN
    public User login(User user) {
        String normalizedEmail = normalizeEmail(user.email());

        UserDao userDao = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        String hashedPassword = Util.hashPassword(user.password(), userDao.getSalt());

        UserDao verifiedUser = userRepository
                .findByEmailAndPassword(userDao.getEmail(), hashedPassword)
                .orElseThrow(InvalidCredentialsException::new);

        return userService.getUser(verifiedUser.getId());
    }
    // LOGOUT
    public void logout() {
        // Implementation for logout
    }


    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
