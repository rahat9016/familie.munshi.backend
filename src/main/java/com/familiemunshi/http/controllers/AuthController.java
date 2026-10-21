package com.familiemunshi.http.controllers;


import com.familiemunshi.common.utils.JwtUtil;
import com.familiemunshi.http.dtos.requests.LoginRequest;
import com.familiemunshi.http.dtos.requests.RefreshTokenRequest;
import com.familiemunshi.http.dtos.requests.SignupRequest;
import com.familiemunshi.http.dtos.responses.RefreshTokenResponse;
import com.familiemunshi.http.dtos.responses.SignupResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.http.dtos.responses.UserLoginResponse;
import com.familiemunshi.service.AuthService;
import com.familiemunshi.service.UserService;
import com.familiemunshi.service.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Authentication", description = "APIs for authentication")
@RestController
@RequestMapping(API_BASE + AUTH)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping(SIGNUP)
    @Operation(summary = "Sign up a new user.x", responses = {@ApiResponse(responseCode = "201", description = "User created."), @ApiResponse(responseCode = "400", description = "Invalid input."), @ApiResponse(responseCode = "401", description = "Unauthenticated."), @ApiResponse(responseCode = "409", description = "User already exists.")})
    public ResponseEntity<SuccessResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        User savedUser = authService.signup(signupRequest.toEntity());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("User registered successfully", new SignupResponse(savedUser)));
    }

    @PostMapping(LOGIN)
    @Operation(summary = "Login user.", responses = {@ApiResponse(responseCode = "200", description = "User logged in."), @ApiResponse(responseCode = "400", description = "Invalid input."), @ApiResponse(responseCode = "401", description = "Invalid credentials.")})
    public ResponseEntity<SuccessResponse<UserLoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        User loggedUser = authService.login(loginRequest.toEntity());
        // generate JWT access token and refresh token
        String accessToken = jwtUtil.generateUserToken(
                loggedUser.id(),
                loggedUser.email()
        );
        String refreshToken = jwtUtil.generateUserRefreshToken(
                loggedUser.id(),
                loggedUser.email()
        );
        return ResponseEntity.ok(SuccessResponse.of("User logged in successfully", new UserLoginResponse(loggedUser.id(), accessToken, refreshToken)));
    }

    @PostMapping(REFRESH_TOKEN)
    @Operation(summary = "Refresh user token.",
            responses = {
            @ApiResponse(responseCode = "200", description = "Token refreshed."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials.")
    })
    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        // VALIDATE REFRESH TOKEN
        try {
            if (!jwtUtil.validateUserRefreshToken(refreshToken)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid or expired refresh token"
                );
            }
        } catch (Exception e){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token"
            );
        }

        UUID userId;
        try {
            userId = jwtUtil.extractUserId(refreshToken);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token"
            );
        }
        User user = userService.getUser(userId);
        String newAccessToken = jwtUtil.generateUserToken(user.id(), user.email());

        return ResponseEntity.ok(SuccessResponse.of("Token refreshed successfully", new RefreshTokenResponse(newAccessToken)));
    }

}
