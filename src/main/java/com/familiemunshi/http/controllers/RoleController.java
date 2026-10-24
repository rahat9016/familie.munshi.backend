package com.familiemunshi.http.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.familiemunshi.common.utils.Constants.API_BASE;
import static com.familiemunshi.common.utils.Constants.ROLES;

@Tag(name = "Roles", description = "APIs for managing roles")
@RestController
@RequestMapping(API_BASE + ROLES)
@AllArgsConstructor
public class RoleController {
    // TODO: Add methods for managing roles
}
