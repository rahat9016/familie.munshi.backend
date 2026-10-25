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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.
                ok(SuccessResponse.
                        of("Role created successfully", new RoleResponse(roleService.createSystemRole(role))));
    }
}
