package com.familiemunshi.http.controllers;


import com.familiemunshi.http.dtos.requests.SignupRequest;
import com.familiemunshi.http.dtos.responses.UserResponse;
import com.familiemunshi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signup(signupRequest)) ;
    }

}
