package com.familiemunshi.http.controllers;


import com.familiemunshi.http.dtos.requests.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Authentication", description = "APIs for authentication")
@RestController
@RequestMapping(API_BASE + AUTH)
public class AuthController {
    @PostMapping(SIGNUP)
    @Operation(
            summary = "Sign up a new user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated.")
            }
    )
    public void createBranch(@Valid @ModelAttribute SignupRequest signupRequest) {
        System.out.println(signupRequest.toString());
    }

}
