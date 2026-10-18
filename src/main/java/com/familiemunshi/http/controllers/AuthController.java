package com.familiemunshi.http.controllers;


import com.familiemunshi.http.dtos.requests.LoginRequest;
import com.familiemunshi.http.dtos.requests.SignupRequest;
import com.familiemunshi.http.dtos.responses.SignupResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.http.dtos.responses.UserResponse;
import com.familiemunshi.service.AuthService;
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

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Authentication", description = "APIs for authentication")
@RestController
@RequestMapping(API_BASE + AUTH)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(SIGNUP)
    @Operation(
            summary = "Sign up a new user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated."),
                    @ApiResponse(responseCode = "409", description = "User already exists.")
            }
    )
    public ResponseEntity<SuccessResponse<SignupResponse>>  signup(@Valid @RequestBody SignupRequest signupRequest) {
        User savedUser = authService.signup(signupRequest.toEntity());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        SuccessResponse.of(
                                "User registered successfully",
                                new SignupResponse(savedUser)
                        )
                );
    }

    @PostMapping(LOGIN)
    @Operation(
            summary = "Login user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User logged in."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input."
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials."
                    )
            }
    )
    public ResponseEntity<SuccessResponse<UserResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        User loggedUser = authService.login(loginRequest.toEntity());
        return ResponseEntity.ok(SuccessResponse.of("User logged in successfully", new UserResponse(loggedUser)));
    }

}
