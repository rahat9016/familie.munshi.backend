package com.familiemunshi.service;

import com.familiemunshi.http.dtos.requests.SignupRequest;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.service.exceptions.UserEmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    // SIGNUP
    public void signup(SignupRequest signupRequest) {
        String normalizedEmail = normalizeEmail(signupRequest.getEmail());

        if(userRepository.findByEmail(normalizedEmail).isPresent()){
            throw new UserEmailAlreadyExistsException(normalizedEmail);
        }

        UserDao userDao = signupRequest.toEntity();

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
