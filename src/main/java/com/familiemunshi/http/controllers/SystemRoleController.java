package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateRoleRequest;
import com.familiemunshi.http.dtos.responses.RoleResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.RoleService;
import com.familiemunshi.service.entities.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
