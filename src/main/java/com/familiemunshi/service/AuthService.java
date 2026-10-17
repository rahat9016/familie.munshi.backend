package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.SignupRequest;
import com.familiemunshi.http.dtos.responses.UserResponse;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.exceptions.UserEmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    // SIGNUP
    public UserResponse signup(SignupRequest signupRequest) {
        User user = signupRequest.toEntity();

        String normalizedEmail = normalizeEmail(signupRequest.getEmail());


        if(userRepository.findByEmail(normalizedEmail).isPresent()){
            throw new UserEmailAlreadyExistsException(normalizedEmail);
        }

        // Hash the password with a salt
        String salt = Util.generateSalt();
        String hashedPassword = Util.hashPassword(signupRequest.getPassword(), salt);

        // Create a new User object with the hashed password
        User hashedUser = user.withPassword(hashedPassword);

        UserDao userDao = UserMapper.toDao(user, salt);

        // Save the user to the database
        UserDao savedUser = userRepository.save(userDao);

        // Return the response
        return new UserResponse(UserMapper.toEntity(savedUser));
    }

    // LOGIN
    public void login() {
        // Implementation for login
    }

    // REFRESH TOKEN
    public void refreshToken() {
        // Implementation for refresh token
    }

    // LOGOUT
    public void logout() {
        // Implementation for logout
    }


    private String normalizeEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
