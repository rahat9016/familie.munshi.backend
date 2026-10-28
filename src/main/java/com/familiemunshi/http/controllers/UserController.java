package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.http.dtos.responses.UserResponse;
import com.familiemunshi.service.UserService;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.filters.FindAllUsersFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping(API_BASE + USERS)
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(ID)
    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'USER_READ')"
    )
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


    @GetMapping
    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'USER_READ')"
    )
    @Operation(
            summary = "Get all users",
            description = "Search and filter users with pagination.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Users retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid pagination or filter parameters"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "USER_READ permission required"
                    )
            }
    )

    public ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers(
            @Valid @ParameterObject
            FindAllUsersFilter filter,

            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1")
            @Min(1)
            int page,

            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<User> users = userService.getAllUsersByFilter(
                filter,
                pageable
        );

        List<UserResponse> response = users.getContent()
                .stream()
                .map(UserResponse::new)
                .toList();

        return ResponseEntity.ok(
                PaginatedResponse.of(
                        "Users retrieved successfully",
                        users,
                        response
                )
        );
    }

}
