package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.AssignUserPlatformRoleRequest;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.http.dtos.responses.UserPlatformRoleResponse;
import com.familiemunshi.service.RoleService;
import com.familiemunshi.service.entities.UserPlatformRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(
        name = "System User Roles",
        description = "APIs for assigning and removing platform roles from users"
)
@RestController
@RequestMapping(API_BASE + SYSTEM + USERS + USER_ID + ROLES)
@RequiredArgsConstructor
public class SystemUserRoleController {

    private final RoleService roleService;

    @PostMapping(consumes = "application/json")
    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'USER_ROLE_ASSIGN')"
    )
    @Operation(
            summary = "Assign a platform role to a user",
            description = "Assigns an existing platform role without removing the user's other roles.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Platform role assigned successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Permission denied"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User or platform role not found"
                    )
            }
    )
    public ResponseEntity<SuccessResponse<UserPlatformRoleResponse>>
    assignUserPlatformRole(
            Authentication authentication,
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody AssignUserPlatformRoleRequest request
    ) {
        UUID callerId = UUID.fromString(authentication.getName());

        UserPlatformRole mapping = roleService.assignUserPlatformRole(
                callerId,
                request.toEntity(userId)
        );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Platform role assigned successfully",
                        new UserPlatformRoleResponse(mapping)
                )
        );
    }

    @DeleteMapping(ROLE_ID)
    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'USER_ROLE_REMOVE')"
    )
    @Operation(
            summary = "Remove a platform role from a user",
            description = "Removes the user-role mapping while keeping the role itself.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Platform role removed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Permission denied"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User or platform role not found"
                    )
            }
    )
    public ResponseEntity<SuccessResponse<UserPlatformRoleResponse>>
    removeUserPlatformRole(
            Authentication authentication,
            @PathVariable("userId") UUID userId,
            @PathVariable("roleId") UUID roleId
    ) {
        UUID callerId = UUID.fromString(authentication.getName());

        UserPlatformRole mapping = roleService.removeUserPlatformRole(
                callerId,
                new UserPlatformRole(userId, roleId)
        );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Platform role removed successfully",
                        new UserPlatformRoleResponse(mapping)
                )
        );
    }
}