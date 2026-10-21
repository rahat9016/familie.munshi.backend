package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.http.dtos.responses.UserResponse;
import com.familiemunshi.service.UserService;
import com.familiemunshi.service.entities.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SuccessResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(SuccessResponse.of("User retrieved successfully", new UserResponse(user)));
    }

    @GetMapping("/")
    public String getAllUsers() {
        return "List of all users.";
    }
}
