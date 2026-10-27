package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateRoleRequest;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.RoleResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.RoleService;
import com.familiemunshi.service.entities.Role;
import com.familiemunshi.service.filters.FindAllRolesFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Roles", description = "APIs for managing roles")
@RestController
@RequestMapping(API_BASE + SYSTEM + ROLES)
@AllArgsConstructor
public class SystemRoleController {
    private final RoleService roleService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<SuccessResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = request.toEntity();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Role created successfully", new RoleResponse(roleService.createSystemRole(role))));
    }

    @GetMapping(ID)
    public ResponseEntity<SuccessResponse<RoleResponse>> getRoleById(@PathVariable("id") String roleId) {
        Role role = roleService.getRoleById(java.util.UUID.fromString(roleId));
        return ResponseEntity.ok(SuccessResponse.of("Role retrieved successfully", new RoleResponse(role)));
    }


    @GetMapping
    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'ROLE_READ')"
    )
    @Operation(
            summary = "Get all system roles",
            description = "Search and filter platform roles with pagination.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Roles retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid pagination or filter values"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "ROLE_READ permission required"
                    )
            }
    )
    public ResponseEntity<PaginatedResponse<RoleResponse>> getAllRoles(

            @Valid @ParameterObject
            FindAllRolesFilter filter,

            @Parameter(
                    description = "Page number starting from 1",
                    example = "1"
            )
            @RequestParam(defaultValue = "1")
            @Min(1)
            int page,

            @Parameter(
                    description = "Number of records per page, maximum 100",
                    example = "20"
            )
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

        Page<Role> roles = roleService.getAllRolesByFilter(
                filter,
                pageable
        );

        List<RoleResponse> response = roles.getContent()
                .stream()
                .map(RoleResponse::new)
                .toList();

        return ResponseEntity.ok(
                PaginatedResponse.of(
                        "Roles retrieved successfully",
                        roles,
                        response
                )
        );
    }
}
