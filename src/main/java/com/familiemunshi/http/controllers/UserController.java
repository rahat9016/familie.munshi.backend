package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.http.dtos.responses.UserResponse;
import com.familiemunshi.service.UserService;
import com.familiemunshi.service.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping(API_BASE + USERS)
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(ID)
    @Operation(summary = "Get user by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials.")
            })
    public ResponseEntity<SuccessResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(SuccessResponse.of("User retrieved successfully", new UserResponse(user)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get user profile.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials.")
            })
    public ResponseEntity<SuccessResponse<UserResponse>> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        User user = userService.getUser(userId);
        return ResponseEntity.ok(SuccessResponse.of("User profile retrieved successfully", new UserResponse(user)));
    }

    @Operation(summary = "Get all users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all users retrieved successfully."),
                    @ApiResponse(responseCode = "400", description = "Invalid input."),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials.")
            })
    @GetMapping("/")
    public String getAllUsers() {
        return "List of all users.";
    }
}
