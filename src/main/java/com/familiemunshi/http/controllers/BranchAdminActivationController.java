
package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.ActivateBranchAdminRequest;
import com.familiemunshi.http.dtos.responses.ActivateBranchAdminResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;

import com.familiemunshi.service.AuthService;
import com.familiemunshi.service.entities.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.familiemunshi.common.utils.Constants.API_BASE;
import static com.familiemunshi.common.utils.Constants.AUTH;

@Tag(
        name = "Authentication",
        description = "Development-only Branch Admin activation"
)
@RestController
@RequestMapping(API_BASE + AUTH)
@RequiredArgsConstructor
public class BranchAdminActivationController {

    private final AuthService authService;

    @PostMapping("/activate-branch-admin")
    @Operation(
            summary = "Activate Branch Admin",
            description = "Development-only password-based activation",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Branch Admin activated"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid new password"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid activation credentials"
                    )
            }
    )
    public ResponseEntity<
            SuccessResponse<ActivateBranchAdminResponse>
            > activateBranchAdmin(
            @Valid @RequestBody
            ActivateBranchAdminRequest request
    ) {

        User activatedAdmin =
                authService.activateBranchAdmin(request);

        ActivateBranchAdminResponse response =
                new ActivateBranchAdminResponse(
                        activatedAdmin.email(),
                        activatedAdmin.isActive()
                );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Branch Admin activated successfully",
                        response
                )
        );
    }
}
